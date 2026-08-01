#!/usr/bin/env bash
set -euo pipefail

SITE_ROOT="${SITE_ROOT:-target/pages-site}"
REPORT_HISTORY_BRANCH="${REPORT_HISTORY_BRANCH:-eventhub-report-history}"
WORKTREE="${RUNNER_TEMP:-/tmp}/eventhub-pages-history-repo"

mkdir -p "${WORKTREE}"
git -C "${WORKTREE}" init
git -C "${WORKTREE}" config user.name "github-actions[bot]"
git -C "${WORKTREE}" config user.email "41898282+github-actions[bot]@users.noreply.github.com"

if [[ -n "${GITHUB_TOKEN:-}" && -n "${GITHUB_REPOSITORY:-}" ]]; then
  git -C "${WORKTREE}" remote add origin "https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git"
else
  git -C "${WORKTREE}" remote add origin "$(git config --get remote.origin.url)"
fi

if git -C "${WORKTREE}" ls-remote --exit-code --heads origin "${REPORT_HISTORY_BRANCH}" >/dev/null 2>&1; then
  git -C "${WORKTREE}" fetch origin "${REPORT_HISTORY_BRANCH}" --depth=1
  git -C "${WORKTREE}" checkout -B "${REPORT_HISTORY_BRANCH}" FETCH_HEAD
else
  git -C "${WORKTREE}" checkout -B "${REPORT_HISTORY_BRANCH}"
fi

rsync -a --delete --exclude ".git" "${SITE_ROOT}/" "${WORKTREE}/"
touch "${WORKTREE}/.nojekyll"

git -C "${WORKTREE}" add -A
if git -C "${WORKTREE}" diff --cached --quiet; then
  echo "No report history changes to persist."
  exit 0
fi

git -C "${WORKTREE}" commit -m "Publish EventHub reports for run ${GITHUB_RUN_ID:-local}-${GITHUB_RUN_ATTEMPT:-1}"
git -C "${WORKTREE}" push origin "HEAD:${REPORT_HISTORY_BRANCH}"
echo "Persisted report history to ${REPORT_HISTORY_BRANCH}."
