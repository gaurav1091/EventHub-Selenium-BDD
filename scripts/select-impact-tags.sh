#!/usr/bin/env bash
set -euo pipefail

AREA=""
BASE_REF="${BASE_REF:-origin/main}"
HEAD_REF="${HEAD_REF:-HEAD}"

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --area)
      AREA="${2:-}"
      shift 2
      ;;
    --base)
      BASE_REF="${2:-}"
      shift 2
      ;;
    --head)
      HEAD_REF="${2:-}"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

mkdir -p target/run-summary

normalize_area() {
  case "$1" in
    auth|events|bookings|admin|api|ux|integration) echo "$1" ;;
    none|"") echo "" ;;
    *)
      echo "Unsupported impact area: $1" >&2
      exit 2
      ;;
  esac
}

areas_for_path() {
  case "$1" in
    src/test/resources/features/auth/*|src/test/java/com/eventhub/automation/pages/LoginPage.java|src/test/java/com/eventhub/automation/steps/AuthSteps.java)
      echo "auth"
      ;;
    src/test/resources/features/events/*|src/test/java/com/eventhub/automation/pages/EventsPage.java|src/test/java/com/eventhub/automation/pages/EventDetailPage.java|src/test/java/com/eventhub/automation/steps/EventSteps.java)
      echo "events"
      ;;
    src/test/resources/features/bookings/*|src/test/java/com/eventhub/automation/pages/BookingsPage.java|src/test/java/com/eventhub/automation/steps/BookingSteps.java|src/test/java/com/eventhub/automation/models/Booking*|src/test/java/com/eventhub/automation/support/CleanupService.java)
      echo "bookings"
      ;;
    src/test/resources/features/admin/*|src/test/java/com/eventhub/automation/pages/AdminEventsPage.java|src/test/java/com/eventhub/automation/steps/AdminSteps.java)
      echo "admin"
      ;;
    src/test/resources/features/api/*|src/test/java/com/eventhub/automation/api/*|src/test/java/com/eventhub/automation/models/*Response.java|src/test/resources/schemas/*|src/test/resources/contracts/*)
      echo "api"
      ;;
    src/test/resources/features/ux/*|src/test/java/com/eventhub/automation/steps/UxSmokeSteps.java)
      echo "ux"
      ;;
    src/test/resources/features/hybrid/*)
      echo "integration"
      ;;
    .github/workflows/*|pom.xml|Dockerfile|docker-compose.yml|scripts/*|src/test/java/com/eventhub/automation/drivers/*|src/test/java/com/eventhub/automation/hooks/*|src/test/java/com/eventhub/automation/listeners/*|src/test/java/com/eventhub/automation/support/*)
      echo "auth events bookings admin api ux integration"
      ;;
  esac
}

area="$(normalize_area "${AREA}")"
changed_files=()
selected_areas=()

if [[ -n "${area}" ]]; then
  selected_areas+=("${area}")
else
  if git rev-parse --verify "${BASE_REF}" >/dev/null 2>&1; then
    while IFS= read -r file; do
      [[ -n "${file}" ]] && changed_files+=("${file}")
    done < <(git diff --name-only "${BASE_REF}...${HEAD_REF}")
  else
    while IFS= read -r file; do
      [[ -n "${file}" ]] && changed_files+=("${file}")
    done < <(git diff --name-only "${HEAD_REF}~1" "${HEAD_REF}" 2>/dev/null || true)
  fi

  for file in "${changed_files[@]}"; do
    for mapped_area in $(areas_for_path "${file}" || true); do
      selected_areas+=("${mapped_area}")
    done
  done
fi

if [[ "${#selected_areas[@]}" -eq 0 ]]; then
  selected_areas=("auth" "events" "bookings" "admin" "api" "ux" "integration")
fi

unique_areas="$(printf "%s\n" "${selected_areas[@]}" | sort -u | tr '\n' ' ' | sed 's/[[:space:]]$//')"
tag_expression="$(printf "%s\n" ${unique_areas} | awk '{ printf "%s@impact-%s", (NR == 1 ? "" : " or "), $1 }')"

{
  echo "{"
  echo "  \"requestedArea\": \"${AREA:-auto}\","
  echo "  \"baseRef\": \"${BASE_REF}\","
  echo "  \"headRef\": \"${HEAD_REF}\","
  echo "  \"selectedAreas\": [$(printf "%s\n" ${unique_areas} | awk '{ printf "%s\"%s\"", (NR == 1 ? "" : ", "), $1 }')],"
  echo "  \"tagExpression\": \"${tag_expression}\""
  echo "}"
} > target/run-summary/impact-selection.json

{
  echo "## Impact Selection"
  echo
  echo "- Requested area: \`${AREA:-auto}\`"
  echo "- Selected areas: \`${unique_areas}\`"
  echo "- Tag expression: \`${tag_expression}\`"
  if [[ "${#changed_files[@]}" -gt 0 ]]; then
    echo "- Changed files considered: \`${#changed_files[@]}\`"
  fi
} > target/run-summary/impact-selection.md

echo "${tag_expression}"
