#!/usr/bin/env bash
set -euo pipefail

BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-true}"
ENVIRONMENT="${ENVIRONMENT:-qa}"
SUITE="${SUITE:-smoke}"
SUITE_NAME="${SUITE_NAME:-${SUITE}}"
PARALLEL="${PARALLEL:-false}"
THREAD_COUNT="${THREAD_COUNT:-2}"
RETRY_COUNT="${RETRY_COUNT:-0}"
RETRY_TAGS="${RETRY_TAGS:-@retryable}"
RETRY_MAX_ALLOWED="${RETRY_MAX_ALLOWED:-0}"
PREFLIGHT_ENABLED="${PREFLIGHT_ENABLED:-true}"
CLEANUP_BEFORE_RUN="${CLEANUP_BEFORE_RUN:-false}"
RUN_ID="${RUN_ID:-}"
IMPACT_AREA="${IMPACT_AREA:-none}"
ACCESSIBILITY_THRESHOLD_ENABLED="${ACCESSIBILITY_THRESHOLD_ENABLED:-false}"
ACCESSIBILITY_MAX_VIOLATIONS="${ACCESSIBILITY_MAX_VIOLATIONS:-0}"
VISUAL_BASELINE_ENABLED="${VISUAL_BASELINE_ENABLED:-false}"
VISUAL_BASELINE_UPDATE="${VISUAL_BASELINE_UPDATE:-false}"
VISUAL_DIFF_MAX_PIXELS="${VISUAL_DIFF_MAX_PIXELS:-0}"

suite_tags() {
  case "$1" in
    all) echo "" ;;
    p0-smoke) echo "@p0 and @smoke" ;;
    p1-regression) echo "@p1 and @regression" ;;
    api-contract) echo "@api and @contract" ;;
    ui-critical) echo "@ui and @critical" ;;
    nightly-stateful) echo "@stateful and @regression" ;;
    accessibility) echo "@accessibility" ;;
    docker-smoke) echo "@docker-smoke or (@p0 and @smoke)" ;;
    *) echo "@$1" ;;
  esac
}

if [[ -n "${CUCUMBER_FILTER_TAGS:-}" ]]; then
  TAG_EXPRESSION="${CUCUMBER_FILTER_TAGS}"
elif [[ "${IMPACT_AREA}" != "none" ]]; then
  TAG_EXPRESSION="$(bash scripts/select-impact-tags.sh --area "${IMPACT_AREA}")"
else
  TAG_EXPRESSION="$(suite_tags "${SUITE}")"
fi

if [[ "${PARALLEL}" == "true" && "${SUITE}" =~ ^(hybrid|stateful|nightly-stateful)$ ]]; then
  echo "ERROR: Suite '${SUITE}' is stateful and must run with PARALLEL=false. Parallel mode adds 'not @stateful', which would execute zero scenarios." >&2
  exit 1
fi

MVN_ARGS=(
  "-Dbrowser=${BROWSER}"
  "-Dheadless=${HEADLESS}"
  "-Denvironment=${ENVIRONMENT}"
  "-Dsuite.name=${SUITE_NAME}"
  "-Dretry.count=${RETRY_COUNT}"
  "-Dretry.tags=${RETRY_TAGS}"
  "-Dretry.max.allowed=${RETRY_MAX_ALLOWED}"
  "-Dpreflight.enabled=${PREFLIGHT_ENABLED}"
  "-Dcleanup.before.run=${CLEANUP_BEFORE_RUN}"
  "-Daccessibility.threshold.enabled=${ACCESSIBILITY_THRESHOLD_ENABLED}"
  "-Daccessibility.max.violations=${ACCESSIBILITY_MAX_VIOLATIONS}"
  "-Dvisual.baseline.enabled=${VISUAL_BASELINE_ENABLED}"
  "-Dvisual.baseline.update=${VISUAL_BASELINE_UPDATE}"
  "-Dvisual.diff.max.pixels=${VISUAL_DIFF_MAX_PIXELS}"
)

if [[ -n "${RUN_ID}" ]]; then
  MVN_ARGS+=("-Drun.id=${RUN_ID}")
fi

if [[ "${PARALLEL}" == "true" ]]; then
  if [[ -z "${TAG_EXPRESSION}" ]]; then
    TAG_EXPRESSION="not @stateful"
  else
    TAG_EXPRESSION="(${TAG_EXPRESSION}) and not @stateful"
  fi
  MVN_ARGS+=(
    "-Dsuite.xml.file=target/test-classes/suites/testng-parallel.xml"
    "-Dparallel=methods"
    "-Dthread.count=${THREAD_COUNT}"
  )
else
  MVN_ARGS+=("-Dparallel=none")
fi

if [[ -n "${TAG_EXPRESSION}" ]]; then
  MVN_ARGS+=("-Dcucumber.filter.tags=${TAG_EXPRESSION}")
fi

if [[ "$#" -gt 0 ]]; then
  "$@"
  exit $?
fi

if [[ "${HEADLESS}" == "true" ]]; then
  mvn test "${MVN_ARGS[@]}"
  bash scripts/assert-scenarios-executed.sh
  exit $?
fi

xvfb-run --auto-servernum --server-args="-screen 0 ${WINDOW_WIDTH:-1440}x${WINDOW_HEIGHT:-1000}x24" \
  mvn test "${MVN_ARGS[@]}"
bash scripts/assert-scenarios-executed.sh
