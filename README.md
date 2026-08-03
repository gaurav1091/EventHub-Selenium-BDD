# EventHub Selenium Java BDD Framework

Modern Selenium Java + Cucumber BDD + TestNG framework built in Maven quickstart style.

## Stack

- Selenium Java with ChromeOptions and FirefoxOptions
- Cucumber BDD with TestNG runner and parallel DataProvider
- Page Object Model with reusable components
- Rest Assured API automation with POJOs and JsonPath validations
- Log4j2 logging
- Extent Spark and Allure reporting
- ThreadLocal WebDriver for parallel execution
- Docker support for headless CI execution
- Failure screenshots only, cleaned at suite start

## Run

Default is headed Chrome:

```bash
mvn test
```

Smoke tests:

```bash
mvn test -Dcucumber.filter.tags="@smoke"
```

Firefox:

```bash
mvn test -Dbrowser=firefox
```

Headless:

```bash
mvn test -Dheadless=true
```

Parallel tuning:

```bash
mvn test -Dsuite.xml.file=target/test-classes/suites/testng-parallel.xml -Dparallel=methods -Dthread.count=4 -Dcucumber.filter.tags="@parallel-safe"
```

Run observability and reliability controls:

```bash
mvn test -Drun.id=local-001 -Dpreflight.enabled=true -Dcleanup.before.run=false
mvn test -Dretry.count=1 -Dretry.tags="@retryable" -Dretry.max.allowed=1 -Dcucumber.filter.tags="@retryable"
bash scripts/assert-scenarios-executed.sh
```

API only:

```bash
mvn test -Dcucumber.filter.tags="@api"
```

Reports:

```bash
mvn allure:serve
```

Extent report is generated at `target/extent-report/EventHub-Cucumber-Report.html` with run ID, environment, browser, headless, parallel, thread count, and tag metadata.

Quality gates:

```bash
mvn -Pquality -DskipTests verify
bash scripts/governance-check.sh
make quarantine-audit
```

Dependency vulnerability scan:

```bash
mvn -Psecurity org.owasp:dependency-check-maven:check
```

## Browser Helpers

You do not need to manually install ChromeDriver or GeckoDriver. The framework uses WebDriverManager, which downloads and wires the correct driver binary for your installed Chrome or Firefox.

Required locally:

- Java 17+; Java 17 is recommended for newer enterprise baselines
- Maven 3.9+
- Google Chrome and/or Mozilla Firefox
- Docker Desktop, only if you want container execution
- Allure CLI, only if you want local `allure serve` outside Maven

On macOS, Allure CLI can be installed with:

```bash
brew install allure
```

## Configuration

Defaults live in `src/test/resources/config/config.properties`. Override with Maven system properties or environment variables:

```bash
mvn test -Duser.email=you@example.com -Duser.password='secret'
USER_EMAIL=you@example.com USER_PASSWORD=secret mvn test
```

The tag and suite strategy is documented in `docs/test-strategy.md`.
Scenario coverage and governance conventions are documented in `docs/test-catalog.md`.
CI usage and failure triage are documented in `docs/ci-runbook.md`.
Developer contribution, debugging, and add-new-test guides live under `docs/`.
GitHub Actions runs a Chrome/Firefox smoke and parallel-safe matrix on push. Pull requests run PR-diff-aware impact selection. Scheduled runs execute a broader nightly regression matrix, while manual dispatch lets you choose browser, suite, impact area, parallel mode, and thread count.

CI publishes a GitHub Pages report dashboard for every trusted workflow run. Each job gets a unique URL under
`runs/<github-run-id>-attempt-<attempt>/<browser-suite-parallel-threads>/` and the page includes browser, suite,
parallel mode, thread count, tag expression, and executed scenario count. The workflow summary prints the dashboard
link after the Pages deployment completes. Historical report folders are persisted on the
`eventhub-report-history` branch so older workflow report links remain available after later CI runs.
The Pages dashboard also publishes cross-run quality trends in `trend-dashboard.html` and `trend-summary.json`.

## Docker

Run the default Docker suite, which uses Chrome, headless mode, and `@smoke`:

```bash
docker compose up --build
```

Run a specific browser and suite:

```bash
BROWSER=firefox SUITE=bookings docker compose run --rm eventhub-tests
```

Run in parallel:

```bash
BROWSER=chrome SUITE=ui PARALLEL=true THREAD_COUNT=4 docker compose run --rm eventhub-tests
```

Run with a virtual display instead of browser headless mode:

```bash
DOCKER_HEADLESS=false BROWSER=chrome SUITE=smoke docker compose run --rm eventhub-tests
```

Run a custom Cucumber tag expression:

```bash
CUCUMBER_FILTER_TAGS="@bookings and not @stateful" docker compose run --rm eventhub-tests
```

Run visual or accessibility smoke:

```bash
make visual
make accessibility
ACCESSIBILITY_THRESHOLD_ENABLED=true ACCESSIBILITY_MAX_VIOLATIONS=0 make accessibility
make accessibility-strict
make visual-baseline
make quarantine-audit
make impact AREA=auth
make impact-select AREA=api
```

The container reads credentials and environment defaults from `.env` through Docker Compose. Reports and failure screenshots are mounted back to `target/`, `logs/`, and `screenshots/` on your machine.
Run summaries are written to `target/run-summary/eventhub-run-summary.json`.
Open `target/run-summary/report-index.html` after a run for one-page links to Extent, Cucumber, Allure results, Axe reports, visual sanity screenshots, governance files, and summaries.
Impact-selected runs also write `target/run-summary/impact-selection.json`.
Retry governance writes `target/run-summary/retry-governance.json`.
Release readiness writes `target/run-summary/release-readiness.json` and `.md`.
Accessibility aggregation writes `target/run-summary/accessibility-summary.json`.
Quarantine governance writes `target/run-summary/quarantine-dashboard.md` and `quarantine-summary.json`.
Visual baseline comparison writes PNG diff images and JSON summaries under `target/visual-diff` when `visual.baseline.enabled=true`.
GitHub Pages publication is assembled by `scripts/build-pages-report.sh`, `scripts/build-pages-site.sh`,
`scripts/restore-pages-history.sh`, and `scripts/persist-pages-history.sh`.

## Troubleshooting

- If VS Code shows Maven project warnings after `pom.xml` changes, reload the Maven project from the Maven sidebar.
- If a browser test fails before opening a page, confirm Chrome/Firefox is installed and rerun with `-Dheadless=true` for CI-like behavior.
- If driver setup fails, delete the WebDriverManager cache or rerun after confirming network access.
- If parallel tests fail only with `@stateful`, run them serially with `-Dparallel=none`.
- If Allure does not open locally, install the Allure CLI or use the generated `target/allure-results` artifact in CI.
