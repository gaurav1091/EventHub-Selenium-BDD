#!/usr/bin/env bash
set -euo pipefail

CUCUMBER_JSON="${CUCUMBER_JSON:-target/cucumber-reports/cucumber.json}"

ruby - "${CUCUMBER_JSON}" <<'RUBY'
require "json"

path = ARGV.fetch(0)
unless File.exist?(path)
  warn "No Cucumber JSON report found at #{path}; cannot verify scenario count."
  exit 1
end

features = JSON.parse(File.read(path))
scenario_count = features.sum do |feature|
  Array(feature["elements"]).count do |element|
    element["type"] == "scenario" || element["keyword"].to_s.include?("Scenario")
  end
end

if scenario_count.zero?
  warn "No Cucumber scenarios were executed. Check suite, tag expression, and parallel/stateful compatibility."
  exit 1
end

puts "Cucumber scenario execution guard passed: #{scenario_count} scenario(s) executed."
RUBY
