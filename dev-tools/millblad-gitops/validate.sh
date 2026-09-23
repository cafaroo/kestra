#!/usr/bin/env bash
set -euo pipefail

# Reusable validation gate for Millblad/Kestra GitOps definition repositories.
# Inspired by leodhb/kestra-gitops, but kept independent of any one repository layout.

FLOWS_DIR="${FLOWS_DIR:-kestra/flows}"
FILES_DIR="${FILES_DIR:-kestra/files}"
KESTRA_IMAGE="${KESTRA_IMAGE:-}"

VALIDATE_ALL=true
FILES_TO_VALIDATE=()
ERRORS=0

usage() {
  cat <<'EOF'
Usage: validate.sh [options] [--files <path>...]

Options:
  --flows-dir <dir>   Flow root. Default: kestra/flows
  --files-dir <dir>   Namespace-files root. Default: kestra/files
  --kestra-image <i>  Optional Kestra image used for syntax validation.
  --files             Validate only following changed paths.
  -h, --help          Show this help.

Environment variables FLOWS_DIR, FILES_DIR and KESTRA_IMAGE provide the
same defaults as the corresponding command-line options.

Conventions:
  <flows-dir>/customer/project/my_flow.yml
      -> namespace: customer.project
      -> id: my_flow

When KESTRA_IMAGE is set, each selected flow is also checked with:
  docker run ... <image> flow validate --local <flow>
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --flows-dir)
      FLOWS_DIR="$2"
      shift 2
      ;;
    --files-dir)
      FILES_DIR="$2"
      shift 2
      ;;
    --kestra-image)
      KESTRA_IMAGE="$2"
      shift 2
      ;;
    --files)
      VALIDATE_ALL=false
      shift
      while [[ $# -gt 0 && "$1" != --* ]]; do
        FILES_TO_VALIDATE+=("$1")
        shift
      done
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

top_level_yaml_value() {
  local file="$1"
  local key="$2"

  awk -v key="$key" '
    $0 ~ ("^" key ":[[:space:]]*") {
      sub("^" key ":[[:space:]]*", "", $0)
      gsub(/^["'"'"']|["'"'"']$/, "", $0)
      print $0
      exit
    }
  ' "$file"
}

flow_files() {
  if [[ "$VALIDATE_ALL" == true ]]; then
    [[ -d "$FLOWS_DIR" ]] || return 0
    find "$FLOWS_DIR" -type f \( -name '*.yml' -o -name '*.yaml' \) -print
    return
  fi

  local file
  for file in "${FILES_TO_VALIDATE[@]}"; do
    if [[ -f "$file" && "$file" == "$FLOWS_DIR"/* && "$file" =~ \.ya?ml$ ]]; then
      printf '%s\n' "$file"
    fi
  done
}

validate_flow_convention() {
  local file="$1"
  local relative="${file#"$FLOWS_DIR"/}"
  local relative_dir
  local expected_namespace
  local expected_id
  local actual_namespace
  local actual_id
  local failed=0

  relative_dir="$(dirname "$relative")"
  expected_namespace="${relative_dir//\//.}"
  [[ "$expected_namespace" == "." ]] && expected_namespace=""

  expected_id="$(basename "$relative")"
  expected_id="${expected_id%.yml}"
  expected_id="${expected_id%.yaml}"

  actual_namespace="$(top_level_yaml_value "$file" namespace)"
  actual_id="$(top_level_yaml_value "$file" id)"

  if [[ "$actual_id" != "$expected_id" ]]; then
    echo "ERROR  $file: id='$actual_id', expected '$expected_id'" >&2
    failed=1
  fi

  if [[ "$actual_namespace" != "$expected_namespace" ]]; then
    echo "ERROR  $file: namespace='$actual_namespace', expected '$expected_namespace'" >&2
    failed=1
  fi

  if [[ "$failed" -eq 0 ]]; then
    echo "OK     $file"
  else
    ERRORS=$((ERRORS + 1))
  fi
}

validate_flow_syntax() {
  local file="$1"

  [[ -n "$KESTRA_IMAGE" ]] || return 0

  if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR  docker is required when KESTRA_IMAGE is set" >&2
    ERRORS=$((ERRORS + 1))
    return
  fi

  local abs_root
  abs_root="$(pwd)"

  if docker run --rm \
      -v "$abs_root:/workspace:ro" \
      "$KESTRA_IMAGE" \
      flow validate --local "/workspace/$file"; then
    echo "OK     syntax $file"
  else
    echo "ERROR  syntax $file" >&2
    ERRORS=$((ERRORS + 1))
  fi
}

mapfile -t SELECTED_FLOWS < <(flow_files)

if [[ "${#SELECTED_FLOWS[@]}" -eq 0 ]]; then
  echo "No flow definitions selected."
  exit 0
fi

echo "Validating ${#SELECTED_FLOWS[@]} flow definition(s)"
echo "Flow root: $FLOWS_DIR"

for file in "${SELECTED_FLOWS[@]}"; do
  validate_flow_convention "$file"
done

# Do not spend container startup time on syntax checks while static invariants fail.
if [[ "$ERRORS" -eq 0 && -n "$KESTRA_IMAGE" ]]; then
  echo
  echo "Running Kestra syntax validation with $KESTRA_IMAGE"
  for file in "${SELECTED_FLOWS[@]}"; do
    validate_flow_syntax "$file"
  done
fi

if [[ "$VALIDATE_ALL" == true && -d "$FILES_DIR" ]]; then
  echo
  echo "Namespace file tree: $FILES_DIR"
  while IFS= read -r file; do
    relative="${file#"$FILES_DIR"/}"
    namespace="$(dirname "$relative")"
    namespace="${namespace//\//.}"
    [[ "$namespace" == "." ]] && namespace=""
    echo "INFO   $file -> namespace '$namespace'"
  done < <(find "$FILES_DIR" -type f -print)
fi

echo
if [[ "$ERRORS" -gt 0 ]]; then
  echo "Validation failed with $ERRORS error(s)." >&2
  exit 1
fi

echo "Validation passed."
