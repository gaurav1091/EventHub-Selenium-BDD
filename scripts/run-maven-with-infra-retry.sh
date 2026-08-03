#!/usr/bin/env bash
set -euo pipefail

mkdir -p target/run-logs
LOG_PREFIX="target/run-logs/maven-$(date +%Y%m%d-%H%M%S)"
LOG_FILE="${LOG_PREFIX}-attempt-1.log"
MAX_ATTEMPTS="${MAVEN_INFRA_MAX_ATTEMPTS:-3}"

run_command() {
  local attempt="$1"
  shift
  LOG_FILE="${LOG_PREFIX}-attempt-${attempt}.log"
  "$@" 2>&1 | tee "${LOG_FILE}"
}

is_infra_failure() {
  grep -Eiq "Could not transfer artifact|Failed to collect dependencies|Failed to read artifact descriptor|status code: (403|408|429|5[0-9][0-9])|reason phrase: (Forbidden|Too Many Requests|Service Unavailable|Gateway Timeout)|Connection reset|Connection timed out|Read timed out|Temporary failure|repo.maven.apache.org|Unknown host|Network is unreachable|SSL peer shut down" "${LOG_FILE}"
}

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
  if run_command "${attempt}" "$@"; then
    exit 0
  fi

  if ! is_infra_failure; then
    exit 1
  fi

  if [[ "${attempt}" -ge "${MAX_ATTEMPTS}" ]]; then
    echo "Detected infrastructure-style failure, but Maven command failed after ${MAX_ATTEMPTS} attempt(s)."
    exit 1
  fi

  sleep_seconds=$((attempt * 10))
  echo "Detected infrastructure-style failure in ${LOG_FILE}. Retrying Maven command in ${sleep_seconds}s..."
  sleep "${sleep_seconds}"
done

exit 1
