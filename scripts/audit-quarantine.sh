#!/usr/bin/env bash
set -euo pipefail

QUARANTINE_FILE="${QUARANTINE_FILE:-src/test/resources/governance/quarantine.json}"
OUTPUT_DIR="${OUTPUT_DIR:-target/governance}"
FAIL_MODE="${QUARANTINE_AUDIT_FAIL:-false}"

mkdir -p "${OUTPUT_DIR}"

ruby -rjson -rdate - "${QUARANTINE_FILE}" "${OUTPUT_DIR}/quarantine-audit.json" "${FAIL_MODE}" <<'RUBY'
file = ARGV.fetch(0)
output = ARGV.fetch(1)
fail_mode = ARGV.fetch(2).casecmp("true").zero?

entries = File.exist?(file) ? JSON.parse(File.read(file)) : []
unless entries.is_a?(Array)
  warn "Quarantine file must contain a JSON array: #{file}"
  exit 1
end

today = Date.today
expired = []
incomplete = []

required = %w[scenario owner reason expiresOn]
entries.each do |entry|
  missing = required.select { |key| entry[key].to_s.strip.empty? }
  incomplete << entry.merge("missing" => missing) unless missing.empty?

  expires_on = entry["expiresOn"].to_s.strip
  next if expires_on.empty?

  begin
    expired << entry if Date.parse(expires_on) < today
  rescue Date::Error
    incomplete << entry.merge("missing" => ["valid expiresOn"])
  end
end

report = {
  "source" => file,
  "count" => entries.size,
  "expiredCount" => expired.size,
  "incompleteCount" => incomplete.size,
  "passed" => expired.empty? && incomplete.empty?,
  "entries" => entries,
  "expired" => expired,
  "incomplete" => incomplete
}

File.write(output, JSON.pretty_generate(report) + "\n")

if fail_mode && !report["passed"]
  warn "Quarantine audit failed: #{expired.size} expired and #{incomplete.size} incomplete entry/entries."
  exit 1
end

puts "Quarantine audit passed: #{entries.size} quarantined scenario(s), #{expired.size} expired, #{incomplete.size} incomplete."
RUBY
