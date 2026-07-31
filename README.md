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
mvn test -Dretry.count=1 -Dretry.tags="@retryable" -Dcucumber.filter.tags="@retryable"
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
GitHub Actions runs a Chrome/Firefox smoke and parallel-safe matrix automatically. Scheduled runs execute a broader nightly regression matrix, while manual dispatch still lets you choose one browser, suite, parallel mode, and thread count.

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

The container reads credentials and environment defaults from `.env` through Docker Compose. Reports and failure screenshots are mounted back to `target/`, `logs/`, and `screenshots/` on your machine.
Run summaries are written to `target/run-summary/eventhub-run-summary.json`.

## Troubleshooting

- If VS Code shows Maven project warnings after `pom.xml` changes, reload the Maven project from the Maven sidebar.
- If a browser test fails before opening a page, confirm Chrome/Firefox is installed and rerun with `-Dheadless=true` for CI-like behavior.
- If driver setup fails, delete the WebDriverManager cache or rerun after confirming network access.
- If parallel tests fail only with `@stateful`, run them serially with `-Dparallel=none`.
- If Allure does not open locally, install the Allure CLI or use the generated `target/allure-results` artifact in CI.
