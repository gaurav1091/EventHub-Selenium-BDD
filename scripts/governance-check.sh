#!/usr/bin/env bash
set -euo pipefail

echo "Running tag governance audit..."
TAG_AUDIT_FAIL=true bash scripts/audit-tags.sh

echo "Checking workflow YAML syntax..."
ruby -e 'require "yaml"; YAML.load_file(".github/workflows/tests.yml"); puts "workflow yaml ok"'
if [[ -f ".github/workflows/static-quality.yml" ]]; then
  ruby -e 'require "yaml"; YAML.load_file(".github/workflows/static-quality.yml"); puts "static quality workflow yaml ok"'
fi

echo "Checking shell script syntax..."
while IFS= read -r script; do
  bash -n "${script}"
done < <(find scripts -name "*.sh" -type f | sort)

echo "Checking impact selector..."
bash scripts/select-impact-tags.sh --area api >/dev/null
ruby -rjson -e 'JSON.parse(File.read("target/run-summary/impact-selection.json")); puts "impact selection json ok"'

echo "Running Maven quality profile..."
mvn -Pquality -DskipTests verify

echo "Governance check passed."
