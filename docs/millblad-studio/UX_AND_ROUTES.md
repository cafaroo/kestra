# Millblad Studio UX and route model

## Route hierarchy

Proposed Central Studio routes:

```text
/:tenant/overview
/:tenant/infrastructure
/:tenant/releases

/:tenant/flows
/:tenant/flows/...
/:tenant/namespaces
/:tenant/namespaces/...
/:tenant/blueprints/...
/:tenant/plugins/...

/:tenant/admin/tenant
```

The existing tenant parameter becomes mandatory after initial redirect.

## Initial navigation

A tenant-less UI route is resolved once:

1. last successfully selected tenant, if still active;
2. configured default tenant;
3. first active tenant;
4. tenant chooser if none exist.

After that, URLs are explicit.

```text
/flows
   ↓
/kviberg/flows
```

Do not silently map tenant-less API requests to `main` in Central Studio mode.

## Sidebar

Expanded shell:

```text
┌───────────────────────────────┐
│ [KV] Kviberggruppen        ▾  │
│      kviberg                  │
├───────────────────────────────┤
│ STUDIO                        │
│ ● Overview                    │
│   Infrastructure              │
│   Releases                    │
│                               │
│ DEFINITIONS                   │
│   Flows                       │
│   Namespaces                  │
│   Blueprints                  │
│   Plugins                     │
│                               │
│ ADMINISTRATION                │
│   Tenant settings             │
└───────────────────────────────┘
```

Runtime navigation entries are removed in definition-only mode, not merely disabled.

## Top navigation

Example:

```text
Kviberggruppen / Infrastructure      [STUDIO · DEFINITIONS ONLY]   Search   ...
```

Infrastructure and Releases may add an environment selector or target action on the right, but tenant selection stays in the sidebar to avoid mixing scopes.

## Overview

### Header

- tenant display name;
- immutable tenant slug;
- active/disabled state;
- Studio definition-only tag.

### Cards

Recommended first row:

1. **Definitions**
   - flows
   - namespaces
   - changed definitions

2. **Source**
   - Forgejo repository
   - branch
   - current commit

3. **Release targets**
   - test health + commit
   - production health + commit

Second row:

- explicit data-boundary card;
- recent release metadata.

Do not add execution volume, failure rate or log charts to the Central Studio overview.

## Infrastructure workflow

### Browse

Default mode is read-only topology.

Toolbar:

```text
Production   [Topology] [Compose]     Fit   +   -                 Edit
```

Canvas shows:

- central definition plane;
- tenant runtime boundary;
- external systems;
- nodes and typed edges.

### Select node

Single click selects a node and opens the right inspector without changing route.

Suggested deep-link query:

```text
/kviberg/infrastructure?target=production&node=kestra
```

This allows browser navigation and bookmarks without introducing a route for every service.

### Edit topology

`Edit` enters an explicit editing state.

Changes affect definition state only. They do not directly mutate the customer runtime.

Actions:

- add service;
- edit Compose configuration;
- edit network;
- edit volume definition;
- connect/disconnect typed edges;
- change image/version;
- review changes.

Save creates/updates the Git-backed desired state. Release is a separate action.

### Node inspector

The panel remains visible beside the canvas.

```text
Kestra runtime                      healthy

General | Compose | Network | Volumes | Secret refs | Changes

Runtime
production

Image
ghcr.io/.../kestra:...

Tenant
main

Network
kviberg_backend

Ports
8080/tcp

Deployed
91fb42c

Desired
91fb42c
```

## Releases workflow

A release is tenant + target + immutable source commit.

### Target selection

Target is never inferred from namespace.

```text
[Test] [Production]
```

### Create release

1. choose target;
2. show currently deployed SHA;
3. show desired/source SHA;
4. summarize changed definition files;
5. validate target;
6. confirm release;
7. create immutable release record;
8. hand off to deployment orchestration;
9. update observed target status.

No execution/customer payloads are copied into Studio.

### Promotion

Promotion chooses a previously validated source commit and applies it to another target.

```text
Test @ 91fb42c
      |
      | Promote same commit
      v
Production @ 91fb42c
```

No YAML rewriting is required because the customer runtime uses its own `main` tenant.

## Tenant switch behavior

### Safe route families

These retain route family:

- overview;
- infrastructure;
- releases;
- flow list;
- namespace list;
- plugins.

### Entity routes

Entity identity must not be assumed to exist in another tenant.

Example:

```text
/kviberg/flows/painting.ata/calculate
              tenant switch → acme
/acme/flows
```

If later we support explicit matching by Git identity, that must be a separate user action.

## Global search

Global search is tenant-scoped by default.

Search results display the current tenant but do not offer cross-tenant results in the same list. A future super-admin customer search is a separate mode/surface.

## Open runtime

Every release target can have a runtime URL.

`Open runtime`:

- opens the customer runtime directly;
- does not proxy through Central Studio;
- may include only authentication/session mechanisms explicitly designed for that runtime;
- does not fetch execution/log data back into Studio.

## Blocked runtime routes

If a legacy/runtime URL is reached in Studio mode, render a purpose-built page rather than an empty table.

Example:

```text
Executions live in the customer runtime

Central Studio does not store or proxy operational customer data.

[Open Kviberg Production runtime]
```

## Responsive behavior

- desktop: sidebar + top bar + split canvas/inspector;
- medium: inspector becomes resizable drawer;
- small/mobile: topology is read-only first; inspector opens full-screen;
- tenant switcher remains reachable from the sidebar/menu at every breakpoint.

Infrastructure editing can remain desktop-only initially if required, but viewing and release status must remain available on smaller screens.
