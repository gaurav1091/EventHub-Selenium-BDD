#!/usr/bin/env bash
set -euo pipefail

FEATURE_DIR="${FEATURE_DIR:-src/test/resources/features}"
OUTPUT_DIR="${OUTPUT_DIR:-target/governance}"
FAIL_MODE="${TAG_AUDIT_FAIL:-false}"

mkdir -p "${OUTPUT_DIR}"

ruby - "${FEATURE_DIR}" "${OUTPUT_DIR}/tag-audit.json" "${FAIL_MODE}" <<'RUBY'
require "json"
require "pathname"

feature_dir = Pathname.new(ARGV.fetch(0))
output_path = Pathname.new(ARGV.fetch(1))
fail_mode = ARGV.fetch(2).casecmp("true").zero?

rules = {
  "suite" => ->(tags) { tags.any? { |tag| %w[@smoke @regression @contract @accessibility @responsive @visual].include?(tag) } },
  "surface" => ->(tags) { tags.any? { |tag| %w[@api @ui @hybrid @accessibility @responsive].include?(tag) } },
  "data_safety" => ->(tags) { tags.any? { |tag| %w[@parallel-safe @stateful].include?(tag) } },
  "priority" => ->(tags) { tags.any? { |tag| tag.match?(/^@p[0-3]$/) } },
  "owner" => ->(tags) { tags.any? { |tag| tag.start_with?("@owner-") } },
  "risk" => ->(tags) { tags.any? { |tag| tag.start_with?("@risk-") } },
  "intent" => ->(tags) { tags.any? { |tag| tag.start_with?("@intent-") } },
  "impact" => ->(tags) { tags.any? { |tag| tag.start_with?("@impact-") } }
}

scenarios = []
Dir.glob(feature_dir.join("**/*.feature")).sort.each do |file|
  feature_tags = []
  pending_tags = []
  feature_name = nil

  File.readlines(file, chomp: true).each_with_index do |line, index|
    stripped = line.strip
    if stripped.start_with?("@")
      pending_tags = stripped.split
      next
    end

    if stripped.start_with?("Feature:")
      feature_tags = pending_tags
      feature_name = stripped.sub("Feature:", "").strip
      pending_tags = []
      next
    end

    next unless stripped.match?(/^Scenario(?: Outline)?:/)

    scenario_name = stripped.sub(/^Scenario(?: Outline)?:/, "").strip
    tags = (feature_tags + pending_tags).uniq.sort
    missing = rules.select { |_name, predicate| !predicate.call(tags) }.keys
    scenarios << {
      "file" => file,
      "line" => index + 1,
      "feature" => feature_name,
      "scenario" => scenario_name,
      "tags" => tags,
      "missing" => missing
    }
    pending_tags = []
  end
end

report = {
  "scenarioCount" => scenarios.size,
  "compliantCount" => scenarios.count { |scenario| scenario["missing"].empty? },
  "nonCompliantCount" => scenarios.count { |scenario| !scenario["missing"].empty? },
  "rules" => rules.keys,
  "scenarios" => scenarios
}

output_path.dirname.mkpath
output_path.write(JSON.pretty_generate(report) + "\n")

if fail_mode && report["nonCompliantCount"].positive?
  warn "Tag audit failed: #{report["nonCompliantCount"]} scenario(s) are missing governance tags."
  scenarios.select { |scenario| !scenario["missing"].empty? }.each do |scenario|
    warn "#{scenario["file"]}:#{scenario["line"]} #{scenario["scenario"]} missing #{scenario["missing"].join(", ")}"
  end
  exit 1
end

puts "Tag audit passed for #{report["compliantCount"]}/#{report["scenarioCount"]} scenarios."
RUBY
