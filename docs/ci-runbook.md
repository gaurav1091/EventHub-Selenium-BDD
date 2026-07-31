# CI Runbook

Use GitHub Actions for repeatable framework checks and use local Maven commands for focused debugging.

## Automatic Runs

Push and pull request runs execute the fast test matrix in `.github/workflows/tests.yml`:

- Chrome smoke, serial.
- Firefox smoke, serial.
- Chrome API, parallel.
- Chrome `@parallel-safe`, parallel with four threads.

Scheduled nightly runs execute broader regression coverage:

- Chrome `@regression and @parallel-safe`.
- Firefox `@regression and @parallel-safe`.
- Chrome API regression.
- Chrome `@stateful` regression serially.

The static quality workflow in `.github/workflows/static-quality.yml` runs:

- Maven Enforcer and Spotless quality gates on push and pull request.
- Tag governance audit on push and pull request.
- OWASP dependency-check on manual or scheduled runs.

Every test run writes `target/run-summary/eventhub-run-summary.json` with run ID, browser, tags, parallel settings, pass/fail counts, retry count, and slowest scenario data.
Runs also write scenario duration, slow-scenario, environment-health, and governance artifacts under `target/run-summary` and `target/governance`.
Runs also write `target/run-summary/report-index.html` as a local index for report navigation.
CI test jobs run Maven through `scripts/run-maven-with-infra-retry.sh`, which retries once only when the Maven log matches infrastructure or dependency-transfer failures.
CI test jobs run `scripts/assert-scenarios-executed.sh` after Maven so a bad tag expression cannot pass with zero scenarios.
GitHub Actions publishes `target/run-summary/github-step-summary.md` into the workflow step summary when it exists.

## Manual Dispatch

Use manual dispatch when you want one targeted run.

Recommended choices:

- `browser=chrome`, `suite=smoke`, `parallel=false` for quick release confidence.
- `browser=firefox`, `suite=smoke`, `parallel=false` for cross-browser confidence.
- `browser=chrome`, `suite=api`, `parallel=true`, `thread-count=3` for API-only validation.
- `browser=chrome`, `suite=parallel-safe`, `parallel=true`, `thread-count=4` for parallel UI/API readiness.
- `browser=chrome`, `suite=stateful`, `parallel=false` for booking/admin mutation scenarios.
- `browser=chrome`, `suite=hybrid`, `parallel=false` for API setup plus UI verification scenarios.
- `browser=chrome`, `suite=ui-critical`, `parallel=false` for critical browser paths.
- `browser=chrome`, `suite=api-contract`, `parallel=true`, `thread-count=2` for API contract coverage.

## Local Equivalents

```bash
mvn test -Dheadless=true -Dbrowser=chrome -Dcucumber.filter.tags="@smoke"
mvn test -Dheadless=true -Dbrowser=firefox -Dcucumber.filter.tags="@smoke"
mvn test -Dheadless=true -Dbrowser=chrome -Dcucumber.filter.tags="@api"
mvn test -Dheadless=true -Dbrowser=chrome -Dsuite.xml.file=target/test-classes/suites/testng-parallel.xml -Dparallel=methods -Dthread.count=4 -Dcucumber.filter.tags="(@parallel-safe) and not @stateful"
mvn test -Dheadless=true -Dbrowser=chrome -Dparallel=none -Dcucumber.filter.tags="@stateful"
```

Reliability controls:

```bash
mvn test -Dpreflight.enabled=true -Dcleanup.before.run=true -Drun.id=ci-debug-001 -Dcucumber.filter.tags="@stateful"
mvn test -Dretry.count=1 -Dretry.tags="@retryable" -Dcucumber.filter.tags="@retryable"
TAG_AUDIT_FAIL=true bash scripts/audit-tags.sh
bash scripts/assert-scenarios-executed.sh
```

Retries are disabled by default. Only scenarios tagged with the configured `retry.tags` value are retried. Use retries for known transient UI/infrastructure noise only, and do not tag deterministic assertion failures as retryable.

Quality gates:

```bash
mvn -Pquality -DskipTests verify
```

Dependency scan:

```bash
mvn -Psecurity org.owasp:dependency-check-maven:check
```

## Failure Triage

- Check the failed scenario and tag first. If it is `@stateful`, reproduce serially.
- Download artifacts for Extent, Allure, Cucumber JSON, Surefire XML, logs, and screenshots.
- Inspect `target/run-summary/eventhub-run-summary.json` for run metadata and retry count.
- Inspect `target/run-summary/slow-scenarios.json` when the suite passes but runtime increases.
- Inspect `target/governance/scenario-governance.json` before merging broad test additions.
- Inspect `target/governance/test-catalog.md` for generated scenario-level traceability.
- For UI failures, inspect the Allure failure metadata attachment for browser, URL, scenario, and tags.
- For accessibility smoke, inspect `target/axe-reports` or the Allure Axe advisory attachment. Axe findings are advisory and do not fail CI by default.
- For thresholded accessibility runs, set `accessibility.threshold.enabled=true` and inspect `target/axe-reports` on failure.
- For visual sanity, inspect `target/visual-sanity` and the Allure screenshot attachment.
- For parallel-only failures, rerun `@parallel-safe` with the same thread count before changing code.
- If a selected run fails before Maven with a stateful-suite/parallel message, rerun with `parallel=false`; this prevents green zero-scenario runs.
- If CI reran automatically, inspect `target/run-logs` to confirm it was an infrastructure retry and not a test retry.
- Treat browser CDP warnings as non-fatal unless a Selenium command also fails.
