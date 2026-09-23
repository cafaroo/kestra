# Millblad Promote

Millblad Promote is the common release/deployment UX for the Millblad Kestra fork.

It deliberately reuses Kestra Promote's product concepts:

- promotion targets
- source/target diff
- explicit promotion gates
- drift status
- promotion history

The transport is different by design:

```text
Millblad Studio
    |
    | Promote
    v
Forgejo
    |
    | immutable release
    v
Customer runtime
    |
    | local import/reconcile
    v
Local Kestra / Compose
```

## Two promotable artefacts

### Automation Release

What the customer should **do**:

- flows
- declared namespace files
- scripts/assets
- Laya decision definitions
- agent/model references
- MCP/plugin requirements
- schemas
- test/eval references
- runtime requirements

Excluded:

- secrets
- real customer data
- RAG documents/chunks
- runtime KV state
- execution state/output

### Runtime Release

What the customer should **have**:

- Compose definition
- Kestra/runtime-api versions and image digests
- Laya/Qdrant/MCP component versions
- resource/network/storage policy
- provided runtime capabilities

## Promotion target

A target belongs to the already-resolved customer/environment context supplied by the separate tenancy architecture.

```yaml
id: production
displayName: Production
environment: production

promotion:
  type: FORGEJO_RELEASE

runtime:
  id: production

confirmationGate: true
```

This module does not implement tenancy or a second tenant registry.

## Drift

Studio compares desired release digests with bounded deployment receipts reported by the customer runtime.

Promotion states:

- IN_SYNC
- OUT_OF_SYNC
- NOT_PROMOTED
- DEPLOYING
- FAILED
- UNREACHABLE

Compatibility is separate:

- COMPATIBLE
- INCOMPATIBLE
- UNKNOWN

## Current implementation slice

The first vertical slice now contains both UI and a minimal backend contract:

- existing Flow `Promote` tab is unlocked and replaced with a Millblad view
- existing `/:tenant?/promote/targets` route is replaced with Millblad Promotion Targets
- existing flows table override extension adds a Deploy status column
- frontend reads targets through the tenant-aware API route abstraction
- `PromotionTargetProvider` is the target-source boundary
- the safe default provider returns no targets rather than inventing deployment targets
- `PromotionCandidateService` creates tenant-context-scoped candidates
- `PromotionCandidateStore` is the persistence boundary
- the first store is in-memory and intentionally replaceable

The tenancy/release-target workstream must supply the real target provider. Promote does not implement a second tenant model.

## Next backend slices

```text
PromotionTarget adapter
PromotionDiff
PromotionCompatibility
PromotionHistory
Automation manifest/resolver
AutomationRelease
RuntimeRelease
PromotionTransport
  -> ForgejoReleaseTransport
```

The runtime remains pull-based; Studio never performs direct Kestra-to-Kestra or Docker transport for Promote.