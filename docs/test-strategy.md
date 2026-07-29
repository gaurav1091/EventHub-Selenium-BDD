# Test Strategy

This framework uses Cucumber tags as the primary test selection contract for local runs, Docker, and GitHub Actions.

## Core Tags

- `@smoke`: fast confidence checks for API and critical UI journeys.
- `@regression`: deeper functional coverage.
- `@api`: API-only scenarios using Rest Assured.
- `@ui`: browser scenarios using Selenium WebDriver.
- `@hybrid`: scenarios that combine API setup or cleanup with UI verification.
- `@stateful`: scenarios that create, mutate, cancel, or clear server-side data.
- `@parallel-safe`: scenarios designed to run concurrently without sharing mutable test state.
- `@negative`: invalid input, unauthorized, or error-path API/UI coverage.

## Parallel Policy

Parallel runs should execute `@parallel-safe` scenarios, or should exclude `@stateful`.

The GitHub Actions workflow and Docker entrypoint automatically add `not @stateful` when `parallel=true`.
This keeps booking/admin data mutation tests serial unless a scenario is explicitly redesigned to be isolated.

## Recommended Runs

```bash
mvn test -Dheadless=true -Dbrowser=chrome -Dcucumber.filter.tags="@smoke"
mvn test -Dheadless=true -Dbrowser=firefox -Dcucumber.filter.tags="@api"
mvn test -Dheadless=true -Dbrowser=chrome -Dsuite.xml.file=target/test-classes/suites/testng-parallel.xml -Dparallel=methods -Dthread.count=4 -Dcucumber.filter.tags="@parallel-safe"
```

Docker examples:

```bash
SUITE=smoke docker compose run --rm eventhub-tests
SUITE=parallel-safe PARALLEL=true THREAD_COUNT=4 docker compose run --rm eventhub-tests
```

## Data Policy

Reusable expected data lives in `src/test/resources/test-data/eventhub-test-data.json`.
Dynamic stateful records should be created through factories with unique names and cleaned through API helpers.
