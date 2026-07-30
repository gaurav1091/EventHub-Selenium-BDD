#!/usr/bin/env bash
set -euo pipefail

mkdir -p target/run-logs
LOG_FILE="target/run-logs/maven-$(date +%Y%m%d-%H%M%S).log"

run_command() {
  "$@" 2>&1 | tee "${LOG_FILE}"
}

is_infra_failure() {
  grep -Eiq "Could not transfer artifact|Connection reset|Connection timed out|Read timed out|Temporary failure|repo.maven.apache.org|Unknown host|Network is unreachable|SSL peer shut down" "${LOG_FILE}"
}

if run_command "$@"; then
  exit 0
fi

if is_infra_failure; then
  echo "Detected infrastructure-style failure. Retrying Maven command once..."
  run_command "$@"
  exit $?
fi

exit 1
