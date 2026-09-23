# Millblad GitOps validation

Reusable pre-promotion validation for Git-backed Kestra definitions.

This takes the useful idea from `leodhb/kestra-gitops` — make the Git
repository layout an explicit contract and validate changed flows before
deployment — but keeps it independent from that repository's wrapper and
deployment model.

## Contract

A flow path maps directly to its Kestra identity:

```
kestra/flows/customer/project/invoice_import.yml

namespace: customer.project
id: invoice_import
```

That makes the Git tree deterministic and gives Millblad Promote a cheap gate
before it creates or advances a release.

## Usage

Validate all definitions using only the static identity checks:

```bash
dev-tools/millblad-gitops/validate.sh
```

Validate staged files only:

```bash
dev-tools/millblad-gitops/validate.sh --files \
  $(git diff --cached --name-only --diff-filter=ACMR)
```

Run Kestra's real flow parser as a second gate by pinning the same image that
the target environment runs:

```bash
KESTRA_IMAGE=kestra/kestra:<version> \
  dev-tools/millblad-gitops/validate.sh
```

Alternative layouts are supported:

```bash
dev-tools/millblad-gitops/validate.sh \
  --flows-dir definitions/flows \
  --files-dir definitions/files
```

## Intended integration

The validator is deliberately transport-agnostic. For Millblad Promote the
expected sequence is:

```
Studio change
  -> Git branch / commit
  -> static definition validation
  -> Kestra syntax validation
  -> review / approval
  -> release target reconciliation
```

It should therefore be reusable from Forgejo CI, local pre-commit hooks and
the Promote backend without coupling validation to GitHub Actions.
