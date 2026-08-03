#!/usr/bin/env bash
set -euo pipefail

SITE_ROOT="${SITE_ROOT:-target/pages-site}"
CURRENT_RUN_KEY="${GITHUB_RUN_ID:-local}-attempt-${GITHUB_RUN_ATTEMPT:-1}"

mkdir -p "${SITE_ROOT}"

ruby -rjson -rcgi -rfileutils -rtime -e '
site = ARGV.fetch(0)
current_run = ARGV.fetch(1)
metadata_files = Dir.glob(File.join(site, "runs", "*", "*", "metadata.json")).sort
entries = metadata_files.map do |file|
  report_dir = File.dirname(file)
  data = JSON.parse(File.read(file))
  data["relativeIndex"] = File.join(report_dir.delete_prefix(site + "/"), "index.html")
  data["releaseReadinessPath"] = File.join(report_dir, "run-summary", "release-readiness.json")
  data["runSummaryPath"] = File.join(report_dir, "run-summary", "eventhub-run-summary.json")
  data["accessibilitySummaryPath"] = File.join(report_dir, "run-summary", "accessibility-summary.json")
  data["visualSummaryPath"] = File.join(report_dir, "run-summary", "visual-quality-summary.json")
  data["retrySummaryPath"] = File.join(report_dir, "run-summary", "retry-governance.json")
  data
end
current_entries = entries.select { |entry| entry["runKey"] == current_run }
latest_path = File.join(site, "latest")
Dir.mkdir(latest_path) unless Dir.exist?(latest_path)

def row(entry, href)
  link = "<a href=\"#{CGI.escapeHTML(href)}\">Open report</a>"
  cells = [
    entry["browser"],
    entry["suite"],
    entry["parallel"],
    entry["threads"],
    entry["testCount"],
    entry["jobName"],
    entry["runKey"],
    entry["generatedAtUtc"]
  ]
  "<tr><td>#{link}</td>" + cells.map { |cell| "<td>#{CGI.escapeHTML(cell.to_s)}</td>" }.join + "</tr>"
end

rows = entries.reverse.map { |entry| row(entry, entry["relativeIndex"]) }.join("\n")
latest_rows = entries.reverse.map { |entry| row(entry, "../#{entry["relativeIndex"]}") }.join("\n")
current_rows = current_entries.map { |entry| row(entry, entry["relativeIndex"]) }.join("\n")
latest_current_rows = current_entries.map { |entry| row(entry, "../#{entry["relativeIndex"]}") }.join("\n")
current_rows = "<tr><td colspan=\"9\">No current run report bundles were found.</td></tr>" if current_rows.empty?
latest_current_rows = "<tr><td colspan=\"9\">No current run report bundles were found.</td></tr>" if latest_current_rows.empty?
rows = "<tr><td colspan=\"9\">No report bundles were found.</td></tr>" if rows.empty?
latest_rows = "<tr><td colspan=\"9\">No report bundles were found.</td></tr>" if latest_rows.empty?
style = "body{font-family:Arial,sans-serif;margin:32px;color:#17202a;line-height:1.5;background:#f8fafc}main{max-width:1200px;margin:0 auto;background:#fff;border:1px solid #d7dee8;border-radius:8px;padding:28px}table{border-collapse:collapse;width:100%;margin-top:12px}th,td{border:1px solid #d9e2ec;padding:10px;text-align:left}th{background:#f1f5f9}a{color:#0f5ea8}"
headers = "<tr><th>Report</th><th>Browser</th><th>Suite</th><th>Parallel</th><th>Threads</th><th>Tests</th><th>Job</th><th>Run</th><th>Generated UTC</th></tr>"

def page(title, intro, headers, rows, style)
  <<~HTML
  <!doctype html>
  <html lang="en">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>#{CGI.escapeHTML(title)}</title>
      <style>#{style}</style>
    </head>
    <body>
      <main>
        <h1>#{CGI.escapeHTML(title)}</h1>
        <p>#{CGI.escapeHTML(intro)}</p>
        <table><thead>#{headers}</thead><tbody>#{rows}</tbody></table>
      </main>
    </body>
  </html>
  HTML
end

def dashboard(title, intro, headers, current_rows, rows, style, trend_prefix)
  <<~HTML
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>#{CGI.escapeHTML(title)}</title>
    <style>#{style}</style>
  </head>
  <body>
    <main>
      <h1>#{CGI.escapeHTML(title)}</h1>
      <p>#{CGI.escapeHTML(intro)}</p>
      <p><a href="#{trend_prefix}trend-dashboard.html">Open quality trends</a> | <a href="#{trend_prefix}trend-summary.json">Open trend JSON</a></p>
      <h2>This Workflow Run</h2>
      <table><thead>#{headers}</thead><tbody>#{current_rows}</tbody></table>
      <h2>Available Runs</h2>
      <table><thead>#{headers}</thead><tbody>#{rows}</tbody></table>
    </main>
  </body>
</html>
HTML
end

index = dashboard(
  "EventHub Selenium BDD Reports",
  "Unique report links are grouped by GitHub run id, run attempt, browser, suite, parallel mode, and thread count.",
  headers,
  current_rows,
  rows,
  style,
  ""
)

latest_index = dashboard(
  "EventHub Selenium BDD Reports",
  "Unique report links are grouped by GitHub run id, run attempt, browser, suite, parallel mode, and thread count.",
  headers,
  latest_current_rows,
  latest_rows,
  style,
  "../"
)

File.write(File.join(site, "index.html"), index)
File.write(File.join(latest_path, "index.html"), latest_index)

entries.group_by { |entry| entry["runKey"] }.each do |run_key, run_entries|
  run_dir = File.join(site, "runs", run_key)
  FileUtils.mkdir_p(run_dir)
  run_rows = run_entries.map { |entry| row(entry, "#{entry["jobSlug"]}/index.html") }.join("\n")
  File.write(
    File.join(run_dir, "index.html"),
    page(
      "EventHub Selenium BDD Report - #{run_key}",
      "This page is unique to GitHub workflow run #{run_key}. It lists only the browser, suite, parallel, thread, and test-count reports generated by this run.",
      headers,
      run_rows,
      style
    )
  )
end

summary = ["### EventHub Published Report Links", ""]
summary << "- [This workflow run](runs/#{current_run}/)"
current_entries.each do |entry|
  summary << "- [#{entry["browser"]} / #{entry["suite"]} / parallel=#{entry["parallel"]} / threads=#{entry["threads"]} / tests=#{entry["testCount"]}](#{entry["relativeIndex"]})"
end
summary << "- No current run report bundles were found." if current_entries.empty?
File.write(File.join(site, "pages-summary.md"), summary.join("\n") + "\n")

manifest = {
  "currentRun" => current_run,
  "currentRunPath" => "runs/#{current_run}/",
  "reports" => current_entries.map do |entry|
    {
      "browser" => entry["browser"],
      "suite" => entry["suite"],
      "parallel" => entry["parallel"],
      "threads" => entry["threads"],
      "testCount" => entry["testCount"],
      "jobName" => entry["jobName"],
      "path" => entry["relativeIndex"]
    }
  end
}
File.write(File.join(site, "pages-summary.json"), JSON.pretty_generate(manifest))

def read_json(path)
  File.exist?(path) ? JSON.parse(File.read(path)) : {}
rescue JSON::ParserError
  {}
end

trend_runs = entries.group_by { |entry| entry["runKey"] }.map do |run_key, run_entries|
  jobs = run_entries.map do |entry|
    run_summary = read_json(entry["runSummaryPath"])
    release = read_json(entry["releaseReadinessPath"])
    accessibility = read_json(entry["accessibilitySummaryPath"])
    visual = read_json(entry["visualSummaryPath"])
    retry_summary = read_json(entry["retrySummaryPath"])
    {
      "browser" => entry["browser"],
      "suite" => entry["suite"],
      "parallel" => entry["parallel"],
      "threads" => entry["threads"],
      "tests" => entry["testCount"].to_i,
      "passed" => run_summary.fetch("passedScenarios", 0).to_i,
      "failed" => run_summary.fetch("failedScenarios", 0).to_i,
      "retried" => run_summary.fetch("retriedScenarios", retry_summary.fetch("retryCount", 0)).to_i,
      "accessibilityViolations" => accessibility.fetch("totalViolations", release.fetch("accessibilityViolations", 0)).to_i,
      "visualDiffPixels" => visual.fetch("totalDifferentPixels", release.fetch("visualDiffPixels", 0)).to_i,
      "visualMissingBaselines" => visual.fetch("missingBaselineCount", release.fetch("visualMissingBaselines", 0)).to_i,
      "releaseStatus" => release.fetch("status", "unknown"),
      "generatedAtUtc" => entry["generatedAtUtc"],
      "path" => entry["relativeIndex"]
    }
  end

  {
    "runKey" => run_key,
    "generatedAtUtc" => jobs.map { |job| job["generatedAtUtc"].to_s }.max.to_s,
    "jobCount" => jobs.size,
    "tests" => jobs.sum { |job| job["tests"] },
    "passed" => jobs.sum { |job| job["passed"] },
    "failed" => jobs.sum { |job| job["failed"] },
    "retried" => jobs.sum { |job| job["retried"] },
    "accessibilityViolations" => jobs.sum { |job| job["accessibilityViolations"] },
    "visualDiffPixels" => jobs.sum { |job| job["visualDiffPixels"] },
    "visualMissingBaselines" => jobs.sum { |job| job["visualMissingBaselines"] },
    "jobs" => jobs
  }
end.sort_by { |run| Time.parse(run["generatedAtUtc"]) rescue Time.at(0) }.reverse

trend_summary = {
  "generatedAtUtc" => Time.now.utc.iso8601,
  "runCount" => trend_runs.size,
  "runs" => trend_runs
}
File.write(File.join(site, "trend-summary.json"), JSON.pretty_generate(trend_summary))

trend_rows = trend_runs.map do |run|
  "<tr><td><a href=\"runs/#{CGI.escapeHTML(run["runKey"])}/\">#{CGI.escapeHTML(run["runKey"])}</a></td>" \
    "<td>#{run["jobCount"]}</td><td>#{run["tests"]}</td><td>#{run["passed"]}</td><td>#{run["failed"]}</td>" \
    "<td>#{run["retried"]}</td><td>#{run["accessibilityViolations"]}</td><td>#{run["visualDiffPixels"]}</td>" \
    "<td>#{run["visualMissingBaselines"]}</td><td>#{CGI.escapeHTML(run["generatedAtUtc"])}</td></tr>"
end.join("\n")
trend_rows = "<tr><td colspan=\"10\">No trend data was found.</td></tr>" if trend_rows.empty?

trend_html = <<~HTML
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EventHub Quality Trends</title>
    <style>#{style}</style>
  </head>
  <body>
    <main>
      <h1>EventHub Quality Trends</h1>
      <p>Cross-run quality signals generated from persisted GitHub Pages report history.</p>
      <table>
        <thead>
          <tr><th>Run</th><th>Jobs</th><th>Tests</th><th>Passed</th><th>Failed</th><th>Retries</th><th>A11y</th><th>Visual Pixels</th><th>Missing Baselines</th><th>Generated UTC</th></tr>
        </thead>
        <tbody>#{trend_rows}</tbody>
      </table>
    </main>
  </body>
</html>
HTML
File.write(File.join(site, "trend-dashboard.html"), trend_html)
' "${SITE_ROOT}" "${CURRENT_RUN_KEY}"

echo "Pages site prepared at ${SITE_ROOT}"
