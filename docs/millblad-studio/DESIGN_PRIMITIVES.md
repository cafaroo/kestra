# Millblad Studio design primitives

These are semantic product primitives built from Kestra's existing design system. They are not a second component library.

## Rules

1. Tenant is always explicit.
2. Environment/release target is never encoded as tenant.
3. Runtime data and definition metadata must look different because they have different trust boundaries.
4. Color communicates state, not customer or environment identity.
5. Every new visual uses existing `--ks-*` tokens.
6. All user-facing strings use vue-i18n.
7. Feature components stay in feature code unless they become genuinely reusable Kestra primitives.

## Scope primitives

### StudioTenantSwitcher

**Purpose:** persistent global customer/tenant scope.

**Base components:** `KsDropdown`, `KsDropdownMenu`, `KsDropdownItem`, `KsIcon`, `KsText`.

**Placement:** sidebar header.

**States:**

- loading;
- tenant selected;
- tenant disabled;
- registry unavailable;
- sidebar collapsed.

**Interaction:**

- tenant switch changes `:tenant` in the route;
- list-level routes keep their route family;
- entity-level routes return to the corresponding list to avoid accidental cross-tenant IDs;
- the selector never changes release target.

### StudioModeIndicator

**Purpose:** persistent reminder that the current instance is the central definition plane.

**Base component:** `KsTag`.

Recommended content:

```text
STUDIO · DEFINITIONS ONLY
```

Use `type="info"` or the closest existing neutral/informational semantic. Do not invent a Millblad-specific color.

### RuntimeBoundaryNotice

**Purpose:** explain why execution/log/runtime-data surfaces are unavailable.

**Base component:** `KsAlert`.

Example content:

```text
Runtime data stays in Kviberggruppen's isolated runtime.
This Studio stores definitions, release metadata and infrastructure configuration only.

[Open runtime]
```

The runtime URL is a release-target property. Studio links to it; Studio does not proxy it.

### TenantIdentity

Compact identity used in overview headers and tenant management.

Fields:

- display name;
- immutable tenant ID;
- status;
- optional customer reference.

Customer branding is not used as a theme color.

## Definition primitives

### DefinitionSummaryCard

**Base:** `KsCard`.

Used for:

- flows;
- namespaces;
- repository;
- source commit;
- pending changes.

Cards show definition counts and metadata only.

### SourceRevision

A small reusable commit representation:

```text
91fb42c  feat(painting): validate journal rows
main     23 Sep 2026 11:42
```

Use mono typography for SHA and branch. Do not invent a Git status color system; standard status semantics remain success/warning/error.

## Release primitives

### ReleaseTargetSelector

**Purpose:** choose test/production/other deployment target within a tenant.

**Base:** `KsTabs type="segmented"` for 2–4 targets, `KsSelect` for larger sets.

Environment is textual: `Test`, `Production`. Do not distinguish environments only by color.

### ReleaseTargetCard

Fields:

- target name;
- runtime URL;
- runtime health;
- deployed Git SHA;
- desired Git SHA;
- drift state;
- last release;
- action: open runtime / release.

### DriftIndicator

Semantic values:

- `synced` → success;
- `pending` → info/neutral;
- `drifted` → warning;
- `blocked` → danger/error.

"Drifted" means desired definition commit and observed deployed commit differ. It says nothing about runtime execution success.

### PromotionLane

Shows immutable commit movement between targets:

```text
Source
  91fb42c
      │
      ▼
Test
  91fb42c  synced
      │
      ▼
Production
  88c413a  update available
```

Promotion never mutates the source definition.

## Infrastructure primitives

Infrastructure uses Vue Flow because Kestra already ships it for the DAG canvas and it supports custom cards, handles, edges, pan/zoom and selection.

### InfrastructureCanvas

**Base:** `@vue-flow/core` + `@vue-flow/background`, following the existing `DagCanvas.vue` pattern.

Capabilities:

- pan/zoom;
- fit-to-view;
- node selection;
- optional drag/reposition in edit mode;
- explicit handles/ports;
- grouped trust boundaries;
- edge labels;
- read-only and edit modes;
- export image later using Kestra's existing screenshot utility.

### TrustBoundaryGroup

A labeled container/zone on the canvas.

Initial group types:

- `studio` — Central Studio, definition plane;
- `runtime` — one isolated customer runtime;
- `external` — customer/external systems.

A boundary is structural, not decoration. Nodes cannot be dragged between groups without an explicit topology change.

The runtime group carries a prominent footer label:

```text
REAL DATA BOUNDARY
```

Central Studio carries:

```text
DEFINITIONS ONLY · NO CUSTOMER RUNTIME DATA
```

### InfrastructureNode

Derived visually from Kestra's existing DAG `AssetNode.vue`, but with infrastructure semantics.

Node anatomy:

```text
┌─────────────────────────────────┐
│ ◉  kestra-runtime       healthy │
│    Kestra 2.1.x                 │
│    runtime / production         │
├─────────────────────────────────┤
│ :8080                 main      │
└─────────────────────────────────┘
```

Node kinds:

- runtime host/LXC;
- service/container;
- database;
- control API;
- repository;
- external system;
- network.

Node state uses existing status colors. Node kind uses icon + text, not a unique color.

### InfrastructurePort

Explicit connection point on a node.

Examples:

- HTTP/API;
- PostgreSQL;
- internal network;
- external connector;
- release input.

Ports are typed so invalid connections can be rejected in edit mode.

### InfrastructureEdge

Edge types:

| Type | Meaning |
| --- | --- |
| definition | Git/release definition movement |
| control | management/API call |
| network | runtime network connectivity |
| dependency | service dependency |
| external | connection to customer/external system |

Runtime customer-data movement is not drawn as an edge from runtime into Studio because that path is forbidden by architecture.

Edges have text labels when protocol or purpose matters: `HTTPS`, `5432`, `compose API`, `release`.

### NodeInspector

Persistent right panel shown when a node is selected.

**Base:** `KsSplitter` panel, not a modal.

Tabs:

- General
- Compose
- Network
- Volumes
- Secret refs
- Changes

The canvas remains visible while inspecting or editing.

Secret refs contain references/identifiers only. Secret values must never render centrally.

### ChangeSetBar

Appears when infrastructure definitions are dirty.

```text
3 unpublished changes                     [Discard] [Review changes]
```

A reviewed change becomes a Git commit/release change, not a direct mutation of the production runtime.

## Visual semantics

Use Kestra semantic tokens only.

| Concept | Token family / component semantic |
| --- | --- |
| normal surface | `--ks-bg-surface` |
| canvas/background | `--ks-bg-base` |
| selected | `--ks-border-focus`, `--ks-text-link` |
| healthy/synced | success |
| warning/drift | warning |
| failed/blocked | error/danger |
| metadata/info | info |
| inactive | neutral / muted |

Spacing follows `--ks-spacing-1..16`; text uses the existing sans and mono families; radii use `--ks-radius-*`.

## Accessibility

- tenant name and environment are always present as text;
- no status is color-only;
- all canvas nodes are keyboard focusable before edit mode ships;
- selected node state has border + focus semantics;
- edge purpose must be available in the inspector even when graph labels are visually truncated;
- respect `prefers-reduced-motion`;
- all dropdowns and node actions use existing accessible Kestra primitives.

## Dark mode

No primitive owns hardcoded theme colors in production code. Static documentation mockups approximate the current light theme only; implementation consumes the existing token set and therefore inherits Kestra light/dark behavior.
