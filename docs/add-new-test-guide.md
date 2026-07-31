# Add New Test Guide

## UI Scenario

1. Add the scenario in the closest feature file under `src/test/resources/features`.
2. Add priority, suite, surface, data-safety, owner, risk, and intent tags.
3. Put browser interactions in a page object under `src/test/java/com/eventhub/automation/pages`.
4. Keep the step definition short and business-readable.
5. Add API cleanup when the scenario creates bookings or events.

Example tags:

```gherkin
@p1 @regression @parallel-safe @critical @intent-discovery @impact-events
Scenario: Event detail deep link loads correct metadata
```

## API Scenario

1. Add or reuse a client method in `EventHubApiClient`.
2. Map stable response payloads to POJOs in `models`.
3. Put JsonPath/schema/POJO assertions in `ApiAssertions`.
4. Use `TestDataFactory` for payloads.
5. Add `@api-cleanup` when the scenario creates server-side data.

## Validation

```bash
make tag-audit
mvn test -Dheadless=true -Dbrowser=chrome -Dcucumber.filter.tags="@your-tag"
make quality
```
