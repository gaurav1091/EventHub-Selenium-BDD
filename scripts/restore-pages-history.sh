#!/usr/bin/env bash
set -euo pipefail

SITE_ROOT="${SITE_ROOT:-target/pages-site}"
REPORT_HISTORY_BRANCH="${REPORT_HISTORY_BRANCH:-eventhub-report-history}"

mkdir -p "${SITE_ROOT}"

if git ls-remote --exit-code --heads origin "${REPORT_HISTORY_BRANCH}" >/dev/null 2>&1; then
  git fetch origin "${REPORT_HISTORY_BRANCH}" --depth=1
  git archive "origin/${REPORT_HISTORY_BRANCH}" | tar -x -C "${SITE_ROOT}"
  echo "Restored report history from ${REPORT_HISTORY_BRANCH}."
else
  echo "No existing ${REPORT_HISTORY_BRANCH} branch found. Starting a new report history site."
fi
