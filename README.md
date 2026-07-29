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
mvn test -Dthread.count=4
```

API only:

```bash
mvn test -Dcucumber.filter.tags="@api"
```

Reports:

```bash
mvn allure:serve
```

Extent report is generated at `target/extent-report/EventHub-Cucumber-Report.html`.

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

## Docker

```bash
docker compose up --build
```
