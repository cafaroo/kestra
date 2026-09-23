# Millblad Studio AI integration

Status: architecture and implementation specification  
Target: `cafaroo/kestra` fork  
Scope: make Kestra Copilot understand and operate tenant-scoped Studio, infrastructure and releases without violating the definition-only boundary.

## Executive summary

Do not build a second AI assistant for Millblad Studio.

Kestra already has the right control plane:

```text
Copilot UI
   |
   | POST /api/v1/{tenant}/ai/threads/{thread}/chat
   v
AiAgentController
   |
   v
AgentOrchestrator
   |
   +--> ModeProfiles
   |      ASK  = READ
   |      PLAN = READ + MUTATE
   |      EDIT = READ + MUTATE + ACT
   |
   +--> ToolCatalog
          |
          +--> native platform tools
          +--> authoring tools
          +--> Kestra docs MCP tools
```

Every Copilot thread is already tenant-scoped. `AgentCallContext.Context` carries the tenant as a managed argument to tools; the model never supplies a tenant parameter.

Millblad Studio should extend that system with:

1. Studio-aware route context.
2. Studio tool availability rules.
3. Infrastructure and release tools.
4. New draft artefact kinds.
5. A Studio system-prompt suffix.
6. Removal of the remaining `main` hardcoding in the legacy one-shot AI controller.

## Existing Kestra AI foundations

### Tenant isolation

The agentic endpoint is already tenant-aware:

```text
/api/v1/{tenant}/ai/threads
```

`AiAgentController` resolves the tenant and creates the thread with that tenant.

`ToolCatalog.dispatch(...)` injects:

```java
AgentCallContext.Context(
    tenant,
    principal,
    providerId,
    conversationId
)
```

into tool methods as a LangChain4j managed argument.

This is the right security shape: tool schemas exposed to the model contain no tenant field.

### Context awareness

The UI already sends free-form, one-turn-only context:

```json
{
  "currentView": {
    "kind": "FLOW",
    "namespace": "painting.ata",
    "flowId": "calculate"
  }
}
```

The backend appends this as the most recent model input and deliberately does not persist it in the thread history.

### Tool policy

Kestra has three tool families:

- `READ`
- `MUTATE`
- `ACT`

and two write policies:

- `AUTO`
- `CONFIRM`

Current modes are cumulative:

```text
ASK   -> READ
PLAN  -> READ + MUTATE
EDIT  -> READ + MUTATE + ACT
```

Authoring tools are a special non-mutating class: they produce validated drafts in every mode, including ASK.

### Confirmation

`ACT + CONFIRM` already provides the behavior we need for release actions.

The model calls the tool once. The orchestrator parks the action, streams a proposed-action card, and only dispatches after explicit approval.

Do not create a second confirmation framework for infrastructure or releases.

## Important compatibility issue

The newer agentic controller is tenant-aware, but the legacy one-shot controller is still hardcoded:

```java
@Controller("/api/v1/main/ai")
public class AiController
```

This endpoint powers flow/dashboard generation paths outside the thread agent.

In a multi-tenant Studio it must become tenant-aware:

```java
@Controller("/api/v1/{tenant}/ai")
```

and continue to resolve the tenant through `TenantService`.

Otherwise a dynamic UI route such as `/api/v1/kviberg/ai/generate/flow` will not resolve even though the new agent endpoints work.

## Studio AI boundary

Central Studio must not expose runtime-data tools.

Today the catalog contains tools such as:

- `list-executions`
- `read-execution`
- `read-execution-logs`
- `restart-execution`

They are valid on a normal Kestra runtime but conceptually invalid in definition-only Central Studio.

Do not merely hide them in the UI. They must be unavailable at catalog dispatch time.

### Add an availability layer

Introduce a separate concern from authorization:

```java
interface AgentToolAvailabilityPolicy {
    boolean isAvailable(
        ToolCatalog.ToolEntry entry,
        AgentCallContext.Context context
    );
}
```

The authoritative dispatch sequence becomes:

```text
tool requested
   |
   +--> mode allows family?
   |
   +--> tool available in this instance mode?
   |
   +--> caller permitted?
   |
   +--> execute
```

`ModeProfiles` also applies availability filtering before advertising tools to save model tokens, but `ToolCatalog.dispatch` remains authoritative.

### Tool domains

Add a semantic domain to platform and authoring tools.

Proposed enum:

```java
enum AgentToolDomain {
    DOCUMENTATION,
    DEFINITION,
    RUNTIME,
    INFRASTRUCTURE,
    RELEASE,
    TENANT_ADMIN
}
```

Central Studio allows:

```text
DOCUMENTATION
DEFINITION
INFRASTRUCTURE
RELEASE
TENANT_ADMIN   (only on the admin surface / when authorized)
```

Customer runtime allows:

```text
DOCUMENTATION
DEFINITION
RUNTIME
```

This is intentionally orthogonal to READ/MUTATE/ACT.

A tool can therefore be:

```text
read-infrastructure
  domain = INFRASTRUCTURE
  family = READ
  policy = AUTO

release-target
  domain = RELEASE
  family = ACT
  policy = CONFIRM
```

## UI context model

Extend frontend-local `ScopeBinding`.

Current kinds include flow, namespace, execution, dashboard, app, test, blueprint and plugin.

Add:

```ts
type StudioScopeKind =
  | "TENANT"
  | "INFRASTRUCTURE"
  | "RELEASES"
  | "RELEASE_TARGET"

interface ScopeBinding {
  kind: ExistingKind | StudioScopeKind
  // existing fields...
  targetId?: string | null
  nodeId?: string | null
  releaseId?: string | null
}
```

### Infrastructure context

On:

```text
/kviberg/infrastructure?target=production&node=kestra
```

send:

```json
{
  "currentView": {
    "kind": "INFRASTRUCTURE",
    "targetId": "production",
    "nodeId": "kestra"
  }
}
```

Do not send Compose bodies, credentials, health payloads or secret values as UI context. The model should call read tools for authoritative state.

### Release context

On:

```text
/kviberg/releases?target=production
```

send:

```json
{
  "currentView": {
    "kind": "RELEASE_TARGET",
    "targetId": "production"
  }
}
```

Desired SHA and deployed SHA are read through a tool, not trusted from the browser context.

### Tenant context

Tenant remains implicit in the thread/API route.

The UI may display a tenant chip to the human, but do not add a model-visible `tenantId` field that a model can mutate or override.

## Tool set

### Existing definition tools to keep

Already present:

```text
READ / AUTO
  list-flows
  read-flow
  validate-flow
  search-plugins
  get-plugin-schema
  docs MCP tools

AUTHORING / AUTO
  author-flow
```

These already operate in the caller's tenant.

### Namespace tools

Add:

```text
list-namespaces
  domain = DEFINITION
  family = READ
  policy = AUTO

read-namespace
  domain = DEFINITION
  family = READ
  policy = AUTO
```

The response should include definition metadata only.

Avoid exposing runtime KV, namespace runtime files or secret values in Studio.

### Infrastructure read tools

```text
read-infrastructure
  domain = INFRASTRUCTURE
  family = READ
  policy = AUTO

read-infrastructure-node
  domain = INFRASTRUCTURE
  family = READ
  policy = AUTO

read-compose-definition
  domain = INFRASTRUCTURE
  family = READ
  policy = AUTO

diff-infrastructure
  domain = INFRASTRUCTURE
  family = READ
  policy = AUTO

validate-infrastructure
  domain = INFRASTRUCTURE
  family = READ
  policy = AUTO
```

These read desired-state and observed deployment metadata from the Infrastructure Layer.

They must not proxy container logs, database content, customer files or secret values into Studio.

### Infrastructure authoring

Add an authoring tool:

```text
author-infrastructure
  kind = AUTHORING
  domain = INFRASTRUCTURE
  save = never
```

It accepts:

- user instructions;
- target ID;
- optional current desired-state definition.

It produces a validated draft.

Extend:

```java
ArtefactKind {
    FLOW,
    DASHBOARD,
    APP,
    INFRASTRUCTURE
}
```

The first version can use YAML as the draft payload so the existing `ArtefactDraft` shape continues to work.

The user applies the draft in the UI. Applying changes desired-state/Git; it does not mutate the running production environment.

### Release read tools

```text
list-release-targets
  domain = RELEASE
  family = READ
  policy = AUTO

read-release-target
  domain = RELEASE
  family = READ
  policy = AUTO

diff-release-target
  domain = RELEASE
  family = READ
  policy = AUTO

list-releases
  domain = RELEASE
  family = READ
  policy = AUTO
```

Typical response:

```json
{
  "target": "production",
  "health": "healthy",
  "desiredCommit": "91fb42c",
  "deployedCommit": "88c413a",
  "drift": "UPDATE_AVAILABLE"
}
```

No execution payload belongs in this result.

### Release action tools

```text
create-release
  domain = RELEASE
  family = ACT
  policy = CONFIRM

promote-release
  domain = RELEASE
  family = ACT
  policy = CONFIRM

rollback-release
  domain = RELEASE
  family = ACT
  policy = CONFIRM
```

`rollback-release` should select an existing immutable source commit, not synthesize an old configuration.

Actions return an acknowledgement/release ID. They must not pretend the deployment is healthy until observed state confirms it.

### Tenant administration

Normal tenant-scoped Copilot should not enumerate or mutate other tenants.

For the global tenant administration screen, optionally add:

```text
list-tenants
  domain = TENANT_ADMIN
  family = READ

create-tenant
  domain = TENANT_ADMIN
  family = ACT
  policy = CONFIRM

disable-tenant
  domain = TENANT_ADMIN
  family = ACT
  policy = CONFIRM
```

These are instance-level administrative tools and must be gated by an explicit admin context/permission, not exposed in ordinary tenant conversations.

Do not add "switch tenant" as an AI tool. Tenant switching is a human UI navigation operation.

## Studio system prompt

Do not fork all of Kestra's mode prompts.

Add a Studio-specific system prompt suffix through a replacement `SystemPromptResolver` or equivalent Studio extension point.

Required guidance:

```text
You are operating in Millblad Central Studio.

- Central Studio is a definition/control plane and contains no customer runtime data.
- The active tenant is supplied by the platform. Never invent, change or ask the model to select a tenant.
- Tenant = customer.
- Namespace = workflow/business domain.
- Release target/environment is separate from tenant and namespace.
- Customer executions, logs, runtime KV, documents and secret values remain inside customer runtimes.
- Never claim to have read runtime customer data from Central Studio.
- Secret references may be handled; secret values must never be returned.
- Infrastructure edits change desired state only.
- Release actions require platform confirmation and must target immutable commits.
```

This suffix should apply to ASK, PLAN and EDIT.

## Copilot UX

Reuse the existing global Copilot dock from `AppTopNavBar.vue`.

No new assistant window is required.

### Context chips

Examples:

```text
Infrastructure: production
Node: kestra

Release target: production

Flow: calculate
Namespace: painting.ata
```

Tenant is always visibly present in the main shell, so the Copilot context does not need a removable tenant chip.

### Scope-aware suggestions

Infrastructure:

- Explain this topology
- What is different from deployed state?
- Add PostgreSQL to this runtime
- Review the network isolation
- Prepare this change for release

Releases:

- What will change in production?
- Is production behind test?
- Prepare a production release
- Explain this release history
- Roll back to the previous release

Overview:

- Summarize pending changes
- What is deployed where?
- Which definitions changed since production?

Flow and Namespace pages keep Kestra's existing authoring behavior.

## Read vs author vs act

The intended user experience is:

```text
"Why is production different from test?"
   -> READ tools only

"Add a Redis service"
   -> author-infrastructure
   -> draft card
   -> user applies desired-state draft

"Release this to production"
   -> diff/read tools
   -> create-release ACT tool
   -> confirmation card
   -> user approves
   -> release orchestration starts
```

AI must never turn an infrastructure authoring request directly into a production mutation.

## Provider layer

Current Kestra OSS `AiServiceManager` accepts only `type: gemini`; it throws for OpenAI, Anthropic, Ollama and other provider types.

The agent/tool architecture itself is provider-neutral because it consumes `AiServiceInterface.streamingChatModel(...)`.

Therefore there are two independent tracks:

### Track A — lowest fork cost

Use Gemini for Central Studio initially.

This lets us implement Studio tools/context without modifying the provider layer.

### Track B — Millblad/local provider

If Central Studio should use OpenAI-compatible or local models, add a provider implementation in the fork against the public `AiServiceInterface` / LangChain4j seam.

Do not copy Kestra Enterprise provider implementations.

A Millblad provider should implement:

- non-streaming `ChatModel` for the existing flow-generation pipeline;
- `StreamingChatModel` for the agent loop;
- provider configuration;
- timeout;
- custom headers;
- optional base URL;
- listeners/telemetry.

Keep this provider work in a separate PR from Studio tool work.

## Thread persistence

AI thread/message repositories are tenant-scoped and therefore fit Central Studio tenancy.

AI conversations are Studio metadata, not customer runtime data.

However users must not paste sensitive customer payloads into Copilot. The platform boundary prevents automatic ingestion, not manual disclosure.

Future hardening may add:

- prompt/content redaction;
- secret-pattern filtering;
- retention controls;
- provider-specific data-handling configuration.

## Implementation map

### Backend files to extend

```text
core/src/main/java/io/kestra/core/ai/agent/models/
  ArtefactKind.java
  AgentToolDomain.java                         NEW

webserver/src/main/java/io/kestra/webserver/services/ai/agent/
  StudioSystemPromptResolver.java              NEW

webserver/src/main/java/io/kestra/webserver/services/ai/agent/tool/
  AiPlatformTool.java                          add domain()
  AiAuthoringTool.java                         add/derive domain()
  ToolCatalog.java                             availability enforcement
  AgentToolAvailabilityPolicy.java             NEW
  DefaultAgentToolAvailabilityPolicy.java      NEW

  ListNamespacesTool.java                      NEW
  ReadNamespaceTool.java                       NEW

  ReadInfrastructureTool.java                  NEW
  ReadInfrastructureNodeTool.java              NEW
  ReadComposeDefinitionTool.java               NEW
  DiffInfrastructureTool.java                  NEW
  ValidateInfrastructureTool.java              NEW
  AuthorInfrastructureTool.java                NEW

  ListReleaseTargetsTool.java                  NEW
  ReadReleaseTargetTool.java                   NEW
  DiffReleaseTargetTool.java                   NEW
  ListReleasesTool.java                        NEW
  CreateReleaseTool.java                       NEW
  PromoteReleaseTool.java                      NEW
  RollbackReleaseTool.java                     NEW
```

Also update:

```text
webserver/.../controllers/api/AiController.java
  /api/v1/main/ai
  ->
  /api/v1/{tenant}/ai
```

### Frontend files

```text
ui/src/components/ai/copilot/types.ts
  add Studio scope kinds/fields

ui/src/components/ai/copilot/routeScope.ts
  map overview/infrastructure/releases

ui/src/components/ai/copilot/CopilotContextChip.vue
  labels for target/node/release

ui/src/components/ai/copilot/
  scope-aware suggestion source

ui/src/components/infrastructure/
  publish selected node + target as Copilot context

ui/src/components/releases/
  publish selected release target as Copilot context
```

## Tests

### Tenant isolation

- a Kviberg Copilot thread cannot read an Acme flow;
- model tool schema contains no tenant argument;
- forged tenant fields inside tool arguments are ignored/rejected because tools use managed context;
- AI thread/message queries are tenant-scoped.

### Studio boundary

- runtime-domain tools are not advertised in Studio;
- direct dispatch of a runtime-domain tool in Studio is rejected;
- execution/log tools are still available on a normal runtime instance;
- Studio system prompt is present for all modes.

### Infrastructure

- read tools return definition/metadata only;
- secret refs are allowed but values are absent;
- authoring creates an unpersisted draft;
- applying a draft changes desired state, not running state.

### Releases

- release reads are AUTO;
- release actions are ACT + CONFIRM;
- no action runs before approval;
- target belongs to the active tenant;
- release uses an immutable commit;
- a successful action means accepted/started, not "deployment healthy".

### UI context

- Infrastructure route sends target/node identity only;
- Release route sends target identity only;
- tenant switch resets unsafe entity context;
- context is not persisted in thread history.

## Recommended delivery sequence

1. Make legacy `AiController` tenant-aware.
2. Add Studio scope kinds and route context.
3. Add tool domains + availability policy.
4. Mark current execution/log/restart tools as `RUNTIME`.
5. Add namespace read tools.
6. Add infrastructure READ tools.
7. Add infrastructure authoring draft.
8. Add release READ tools.
9. Add release ACT + CONFIRM tools.
10. Add Studio prompt suffix.
11. Add scope-aware suggestions.
12. Add tenant-admin AI only after an explicit admin authorization model exists.

This keeps Copilot useful from the first Studio screen while preserving the no-runtime-data boundary by construction.
