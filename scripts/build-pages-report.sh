#!/usr/bin/env bash
set -euo pipefail

slugify() {
  ruby -e 'value = ARGV.fetch(0).downcase.gsub(/[^a-z0-9]+/, "-").gsub(/^-|-$/, ""); puts(value.empty? ? "unknown" : value)' "$1"
}

RUN_KEY="${GITHUB_RUN_ID:-local}-attempt-${GITHUB_RUN_ATTEMPT:-1}"
BROWSER="${REPORT_BROWSER:-${BROWSER:-${browser:-unknown}}}"
SUITE="${REPORT_SUITE:-${SUITE_NAME:-${SUITE:-unknown}}}"
PARALLEL="${REPORT_PARALLEL:-${PARALLEL:-false}}"
THREAD_COUNT="${REPORT_THREAD_COUNT:-${THREAD_COUNT:-${thread_count:-1}}}"
JOB_NAME="${REPORT_JOB_NAME:-${GITHUB_JOB:-local}}"
TAG_EXPRESSION="${REPORT_TAG_EXPRESSION:-${CUCUMBER_FILTER_TAGS:-}}"
JOB_SLUG="$(slugify "${JOB_NAME}-${BROWSER}-${SUITE}-parallel-${PARALLEL}-threads-${THREAD_COUNT}")"
REPORT_ROOT="${REPORT_ROOT:-target/pages-report}"
PAGE_DIR="${REPORT_ROOT}/runs/${RUN_KEY}/${JOB_SLUG}"
SUMMARY_JSON="target/run-summary/eventhub-run-summary.json"

mkdir -p "${PAGE_DIR}" target/run-summary

TEST_COUNT="$(ruby -rjson -e '
path = ARGV.fetch(0)
if File.exist?(path)
  summary = JSON.parse(File.read(path))
  total = summary["totalScenarios"] || summary["totalTests"] || summary["total"]
  total ||= summary["passedScenarios"].to_i + summary["failedScenarios"].to_i
  puts total.to_i
else
  puts ENV.fetch("REPORT_TEST_COUNT", "0")
end
' "${SUMMARY_JSON}")"

PAGES_BASE_URL="${PAGES_BASE_URL:-}"
if [[ -z "${PAGES_BASE_URL}" && -n "${GITHUB_REPOSITORY:-}" ]]; then
  OWNER="${GITHUB_REPOSITORY%%/*}"
  REPO="${GITHUB_REPOSITORY#*/}"
  PAGES_BASE_URL="https://${OWNER}.github.io/${REPO}/"
fi
PAGES_BASE_URL="${PAGES_BASE_URL%/}/"
REPORT_PATH="runs/${RUN_KEY}/${JOB_SLUG}/"
REPORT_URL="${PAGES_BASE_URL}${REPORT_PATH}"

copy_dir() {
  local source_dir="$1"
  local target_name="$2"
  if [[ -d "${source_dir}" ]]; then
    mkdir -p "${PAGE_DIR}/${target_name}"
    cp -R "${source_dir}/." "${PAGE_DIR}/${target_name}/"
  fi
}

copy_dir "target/cucumber-reports" "cucumber-reports"
copy_dir "target/extent-report" "extent-report"
copy_dir "target/allure-results" "allure-results"
copy_dir "target/axe-reports" "axe-reports"
copy_dir "target/visual-sanity" "visual-sanity"
copy_dir "target/visual-diff" "visual-diff"
copy_dir "target/governance" "governance"
copy_dir "target/run-summary" "run-summary"
copy_dir "target/run-logs" "run-logs"
copy_dir "target/surefire-reports" "surefire-reports"
copy_dir "screenshots" "screenshots"

export RUN_KEY REPORT_PATH REPORT_URL BROWSER SUITE PARALLEL THREAD_COUNT TEST_COUNT JOB_NAME JOB_SLUG TAG_EXPRESSION
export GITHUB_REPOSITORY="${GITHUB_REPOSITORY:-local}"
export GITHUB_WORKFLOW="${GITHUB_WORKFLOW:-local}"
export GITHUB_RUN_ID="${GITHUB_RUN_ID:-local}"
export GITHUB_RUN_ATTEMPT="${GITHUB_RUN_ATTEMPT:-1}"
export GITHUB_SHA="${GITHUB_SHA:-local}"
export GITHUB_REF_NAME="${GITHUB_REF_NAME:-local}"

ruby -rjson -rtime -e '
metadata = {
  "runKey" => ENV.fetch("RUN_KEY"),
  "reportPath" => ENV.fetch("REPORT_PATH"),
  "reportUrl" => ENV.fetch("REPORT_URL"),
  "browser" => ENV.fetch("BROWSER"),
  "suite" => ENV.fetch("SUITE"),
  "parallel" => ENV.fetch("PARALLEL"),
  "threads" => ENV.fetch("THREAD_COUNT"),
  "testCount" => ENV.fetch("TEST_COUNT").to_i,
  "jobName" => ENV.fetch("JOB_NAME"),
  "jobSlug" => ENV.fetch("JOB_SLUG"),
  "tagExpression" => ENV.fetch("TAG_EXPRESSION", ""),
  "repository" => ENV.fetch("GITHUB_REPOSITORY"),
  "workflow" => ENV.fetch("GITHUB_WORKFLOW"),
  "githubRunId" => ENV.fetch("GITHUB_RUN_ID"),
  "githubRunAttempt" => ENV.fetch("GITHUB_RUN_ATTEMPT"),
  "commitSha" => ENV.fetch("GITHUB_SHA"),
  "refName" => ENV.fetch("GITHUB_REF_NAME"),
  "generatedAtUtc" => Time.now.utc.iso8601
}
File.write(File.join(ARGV.fetch(0), "metadata.json"), JSON.pretty_generate(metadata))
' "${PAGE_DIR}"

ruby -rjson -rcgi -e '
dir = ARGV.fetch(0)
metadata = JSON.parse(File.read(File.join(dir, "metadata.json")))
links = [
  ["Extent Report", "extent-report/EventHub-Cucumber-Report.html"],
  ["Cucumber HTML", "cucumber-reports/cucumber.html"],
  ["Run Summary", "run-summary/eventhub-run-summary.json"],
  ["Run Summary Index", "run-summary/report-index.html"],
  ["GitHub Step Summary", "run-summary/github-step-summary.md"],
  ["Scenario Durations", "run-summary/scenario-durations.json"],
  ["Slow Scenarios", "run-summary/slow-scenarios.json"],
  ["Retry Governance", "run-summary/retry-governance.json"],
  ["Release Readiness", "run-summary/release-readiness.md"],
  ["Release Readiness JSON", "run-summary/release-readiness.json"],
  ["Accessibility Summary", "run-summary/accessibility-summary.json"],
  ["Visual Quality Summary", "run-summary/visual-quality-summary.json"],
  ["Quarantine Dashboard", "run-summary/quarantine-dashboard.md"],
  ["Quarantine Summary", "run-summary/quarantine-summary.json"],
  ["Impact Selection", "run-summary/impact-selection.json"],
  ["Environment Health", "run-summary/environment-health.json"],
  ["Governance", "governance/scenario-governance.json"],
  ["Test Catalog", "governance/test-catalog.md"],
  ["Axe Reports", "axe-reports/"],
  ["Visual Sanity", "visual-sanity/"],
  ["Visual Diffs", "visual-diff/"],
  ["Surefire Reports", "surefire-reports/"],
  ["Screenshots", "screenshots/"]
].select { |_, href| File.exist?(File.join(dir, href.sub(%r{/$}, ""))) }
rows = metadata.map do |key, value|
  "<tr><th>#{CGI.escapeHTML(key)}</th><td><code>#{CGI.escapeHTML(value.to_s)}</code></td></tr>"
end.join("\n")
link_rows = links.map do |name, href|
  "<tr><td>#{CGI.escapeHTML(name)}</td><td><a href=\"#{CGI.escapeHTML(href)}\">#{CGI.escapeHTML(href)}</a></td></tr>"
end.join("\n")
html = <<~HTML
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EventHub CI Report - #{CGI.escapeHTML(metadata["suite"])} - #{CGI.escapeHTML(metadata["browser"])}</title>
    <style>
      body{font-family:Arial,sans-serif;margin:32px;color:#17202a;line-height:1.5;background:#f8fafc}
      main{max-width:1120px;margin:0 auto;background:#fff;border:1px solid #d7dee8;border-radius:8px;padding:28px}
      h1{margin:0 0 8px;font-size:28px}
      h2{margin-top:28px;font-size:20px}
      table{border-collapse:collapse;width:100%;margin-top:12px}
      th,td{border:1px solid #d9e2ec;padding:10px;text-align:left;vertical-align:top}
      th{width:220px;background:#f1f5f9}
      code{white-space:pre-wrap}
      a{color:#0f5ea8}
      .pill{display:inline-block;border:1px solid #cbd5e1;border-radius:999px;padding:4px 10px;margin-right:6px;background:#f8fafc}
    </style>
  </head>
  <body>
    <main>
      <h1>EventHub CI Report</h1>
      <p>
        <span class="pill">Browser: #{CGI.escapeHTML(metadata["browser"])}</span>
        <span class="pill">Suite: #{CGI.escapeHTML(metadata["suite"])}</span>
        <span class="pill">Parallel: #{CGI.escapeHTML(metadata["parallel"])}</span>
        <span class="pill">Threads: #{CGI.escapeHTML(metadata["threads"])}</span>
        <span class="pill">Tests: #{CGI.escapeHTML(metadata["testCount"].to_s)}</span>
      </p>
      <h2>Run Metadata</h2>
      <table><tbody>#{rows}</tbody></table>
      <h2>Report Artifacts</h2>
      <table><thead><tr><th>Artifact</th><th>Link</th></tr></thead><tbody>#{link_rows}</tbody></table>
    </main>
  </body>
</html>
HTML
File.write(File.join(dir, "index.html"), html)
' "${PAGE_DIR}"

printf '%s\n' "${REPORT_URL}" > target/run-summary/pages-report-url.txt
cat > target/run-summary/pages-report-url.md <<EOF
### EventHub Published Report

- Report URL: [${REPORT_URL}](${REPORT_URL})
- Browser: \`${BROWSER}\`
- Suite: \`${SUITE}\`
- Parallel: \`${PARALLEL}\`
- Threads: \`${THREAD_COUNT}\`
- Tests: \`${TEST_COUNT}\`
- Report path: \`${REPORT_PATH}\`
EOF

cp "${PAGE_DIR}/metadata.json" target/run-summary/pages-report-metadata.json

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  cat target/run-summary/pages-report-url.md >> "${GITHUB_STEP_SUMMARY}"
fi

echo "Pages report prepared at ${PAGE_DIR}"
echo "Expected report URL: ${REPORT_URL}"
