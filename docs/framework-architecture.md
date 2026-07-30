# Framework Architecture

The framework separates responsibilities into small, testable layers:

- `config`: typed configuration with system property and environment overrides.
- `drivers`: browser creation and ThreadLocal WebDriver lifecycle.
- `pages`: Selenium Page Object Model classes.
- `api`: Rest Assured client and JsonPath assertions.
- `models`: POJOs/records for API payloads and responses.
- `factories`: disposable test data builders.
- `hooks`: Cucumber browser lifecycle and failure attachments.
- `listeners`: TestNG suite lifecycle and screenshot cleanup.
- `runners`: Cucumber-TestNG bridge with parallel DataProvider.
- `utils`: waits, screenshots, and reusable UI assertion helpers.

The reference projects were used for EventHub flows, selectors, endpoint routes, scenario coverage, tagging strategy, and report expectations.
