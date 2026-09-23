# Millblad Studio implementation map

This document maps the UX proposal onto the current Kestra OSS codebase.

## Existing foundations to reuse

### Shell

- `ui/src/components/layout/SideBar.vue`
- `ui/src/components/layout/AppTopNavBar.vue`
- `ui/src/components/layout/TopNavBar.vue`
- `ui/src/routes/routes.ts`

### Design system

- `KsSideBar`
- `KsTopNavBar`
- `KsCard`
- `KsTag`
- `KsAlert`
- `KsTabs`
- `KsSelect`
- `KsSplitter`
- `KsDrawer`
- `KsDataTable`

### Graph/editor foundation

- `ui/src/components/dependencies/components/dag/DagCanvas.vue`
- `ui/src/components/dependencies/components/dag/AssetNode.vue`
- `ui/src/components/dependencies/components/dag/DagToolbar.vue`
- `@vue-flow/core`
- `@vue-flow/background`
- Kestra topology screenshot helpers

The existing DAG canvas is currently read-only. Infrastructure should derive a separate feature component from the pattern rather than modifying dependency DAG semantics into a generic editor.

## Proposed feature tree

```text
ui/src/
├── components/
│   ├── studio/
│   │   ├── StudioTenantSwitcher.vue
│   │   ├── StudioModeIndicator.vue
│   │   ├── RuntimeBoundaryNotice.vue
│   │   ├── TenantOverview.vue
│   │   └── TenantSourceRevision.vue
│   │
│   ├── infrastructure/
│   │   ├── InfrastructurePage.vue
│   │   ├── InfrastructureCanvas.vue
│   │   ├── InfrastructureToolbar.vue
│   │   ├── InfrastructureNode.vue
│   │   ├── TrustBoundaryGroup.vue
│   │   ├── NodeInspector.vue
│   │   ├── ChangeSetBar.vue
│   │   ├── types.ts
│   │   └── composables/
│   │       ├── useInfrastructureGraph.ts
│   │       └── useInfrastructureSelection.ts
│   │
│   └── releases/
│       ├── ReleasesPage.vue
│       ├── ReleaseTargetSelector.vue
│       ├── ReleaseTargetCard.vue
│       ├── PromotionLane.vue
│       └── ReleaseHistory.vue
│
├── stores/
│   ├── tenants.ts
│   ├── infrastructure.ts
│   └── releases.ts
│
└── composables/
    └── useStudioMode.ts
```

If a component turns out to be generic for Kestra beyond Studio, promote it into `ui/packages/design-system` only then. Do not prefix feature components with `Ks`.

## Shell modifications

### SideBar.vue

Add a tenant-switcher slot/feature to the header.

Current header contains the collapse control. Proposed expanded order:

```text
StudioTenantSwitcher     collapse
```

Collapsed sidebar renders a compact tenant identity button.

### useLeftMenu

In Studio definition-only mode:

Add:

- Overview
- Infrastructure
- Releases

Keep definition surfaces:

- Flows
- Namespaces
- Blueprints
- Plugins

Hide runtime-data surfaces:

- Executions
- Logs
- runtime KV
- runtime output/file navigation
- secret values

Do not hardcode this behavior in every menu item. Expose a Studio-mode capability/visibility rule.

### AppTopNavBar.vue

Inject `StudioModeIndicator` into the action area when Studio mode is enabled.

Do not move tenant selection into the top bar.

## Tenant store

Proposed state:

```ts
type TenantSummary = {
  id: string
  name: string
  status: "ACTIVE" | "DISABLED"
}

state:
  tenants
  activeTenantId
  loading
  error
```

Responsibilities:

- load registry;
- validate current route tenant;
- expose active tenant;
- switch tenant safely;
- resolve default tenant for tenant-less UI entry.

Do not store release target in the same state field.

## Infrastructure model

Initial UI DTO:

```ts
type InfrastructureNode = {
  id: string
  kind: "runtime" | "service" | "database" | "repository" | "control" | "external" | "network"
  name: string
  groupId: string
  status?: "healthy" | "warning" | "failed" | "unknown"
  image?: string
  version?: string
  ports?: InfrastructurePort[]
  metadata?: Record<string, string>
}

type InfrastructurePort = {
  id: string
  kind: "http" | "database" | "network" | "release" | "external"
  label?: string
}

type InfrastructureEdge = {
  id: string
  source: string
  sourcePort?: string
  target: string
  targetPort?: string
  kind: "definition" | "control" | "network" | "dependency" | "external"
  label?: string
}

type TrustBoundary = {
  id: string
  kind: "studio" | "runtime" | "external"
  title: string
  subtitle?: string
}
```

The DTO is definition-plane metadata. It must not contain customer payload data or secret values.

## Vue Flow implementation notes

Reuse patterns from `DagCanvas.vue`:

- `VueFlow`;
- theme-aware `Background`;
- `useVueFlow`;
- fit/zoom behavior;
- screenshot/export helper;
- custom node slots.

Infrastructure-specific differences:

- nodes may become draggable in edit mode;
- nodes use explicit typed handles;
- selectable elements are enabled;
- trust-boundary group nodes are used;
- topology state can be edited;
- changes are staged before Git/release actions.

Do not overload the existing dependency `AssetNode`; create `InfrastructureNode` with the same visual grammar.

## Release pages

Use:

- `KsTabs type="segmented"` for target switching;
- `KsCard` for current target state;
- `KsDataTable` for history;
- `KsTag` for health/drift;
- `KsDrawer` for release review/confirmation if a side workflow is preferable to a full route.

Release status and runtime health are separate fields.

## Token usage

Production styles use only Kestra tokens from:

- `ui/packages/design-system/src/assets/styles/ks-tokens.scss`
- active theme files.

Typical tokens:

```css
background: var(--ks-bg-surface);
border: 1px solid var(--ks-border-default);
color: var(--ks-text-primary);
gap: var(--ks-spacing-3);
border-radius: var(--ks-radius-base);
```

Status styling uses existing status semantics.

Do not add hardcoded hex colors to feature components.

## Tests

### Tenant switcher

- active tenant rendered from route/store;
- switch list route preserves route family;
- switch entity route drops entity identity;
- disabled tenant cannot be selected;
- collapsed sidebar remains operable.

### Studio mode

- executions/log/runtime KV entries absent from menu;
- runtime route guard renders boundary notice;
- top bar indicator present;
- no runtime-data API call is emitted from blocked pages.

### Infrastructure

- nodes render by DTO kind;
- selecting node opens inspector;
- query-state selection round-trips;
- cross-boundary invalid connection rejected;
- secret ref renders identifier only;
- edit state produces definition diff, not direct runtime mutation.

### Releases

- target is independent of tenant;
- source/deployed SHA drift states are correct;
- promotion keeps the same immutable SHA;
- opening runtime uses runtime URL and no proxy call.

## Storybook

Add stories for:

- `StudioTenantSwitcher`;
- `InfrastructureNode`;
- `TrustBoundaryGroup`;
- `ReleaseTargetCard`.

Only promote these into the design-system Storybook if they are moved into the design system. Feature-level stories can live with existing UI Storybook tests.

## Suggested implementation sequence

1. tenant store + switcher + route behavior;
2. Studio mode indicator + hidden runtime navigation;
3. overview shell;
4. infrastructure read-only canvas + inspector;
5. infrastructure edit mode;
6. release targets + release history;
7. Git-backed change/release actions.

This keeps the first implementation aligned with the tenant foundation before adding infrastructure mutation.
