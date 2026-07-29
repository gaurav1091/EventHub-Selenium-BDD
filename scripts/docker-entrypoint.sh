#!/usr/bin/env bash
set -euo pipefail

BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-true}"
ENVIRONMENT="${ENVIRONMENT:-qa}"
SUITE="${SUITE:-smoke}"
PARALLEL="${PARALLEL:-false}"
THREAD_COUNT="${THREAD_COUNT:-2}"

if [[ -n "${CUCUMBER_FILTER_TAGS:-}" ]]; then
  TAG_EXPRESSION="${CUCUMBER_FILTER_TAGS}"
elif [[ "${SUITE}" == "all" ]]; then
  TAG_EXPRESSION=""
else
  TAG_EXPRESSION="@${SUITE}"
fi

MVN_ARGS=(
  "-Dbrowser=${BROWSER}"
  "-Dheadless=${HEADLESS}"
  "-Denvironment=${ENVIRONMENT}"
)

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
  exec "$@"
fi

if [[ "${HEADLESS}" == "true" ]]; then
  exec mvn test "${MVN_ARGS[@]}"
fi

exec xvfb-run --auto-servernum --server-args="-screen 0 ${WINDOW_WIDTH:-1440}x${WINDOW_HEIGHT:-1000}x24" \
  mvn test "${MVN_ARGS[@]}"
