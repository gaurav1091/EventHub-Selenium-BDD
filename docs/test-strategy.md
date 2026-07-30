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

## Tag Governance

Every scenario should have one scope tag and at least one execution-purpose tag.

Scope tags:

- Use `@api` for scenarios that never start a browser.
- Use `@ui` for Selenium browser scenarios.
- Use `@hybrid` when API setup/cleanup and UI verification are both part of the scenario.

Execution-purpose tags:

- Use `@smoke` only for fast release-confidence checks.
- Use `@regression` for deeper coverage that can run nightly or before larger releases.
- Use `@negative` with `@regression` for invalid input or authorization/error-path checks.

Parallel-state tags:

- Use `@parallel-safe` only when the scenario can run at the same time as another scenario without relying on shared mutable server data.
- Use `@stateful` when the scenario creates, updates, cancels, or clears bookings/events/users on the shared EventHub environment.
- Do not combine `@stateful` and `@parallel-safe` unless the data is isolated by run ID and the cleanup path is proven independent.

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

Manual GitHub Actions dispatch supports these choices:

- Browser: `chrome`, `firefox`
- Suite: `smoke`, `ui`, `api`, `regression`, `admin`, `auth`, `bookings`, `events`, `navigation`, `hybrid`, `stateful`, `parallel-safe`, `all`
- Parallel: `true`, `false`
- Thread count: `2`, `3`, `4`, `5`

## Data Policy

Reusable expected data lives in `src/test/resources/test-data/eventhub-test-data.json`.
Dynamic stateful records should be created through factories with unique names and cleaned through API helpers.

## CI Policy

Push and pull request runs execute a fast matrix:

- Chrome smoke, serial.
- Firefox smoke, serial.
- Chrome API, parallel.
- Chrome `@parallel-safe`, parallel with four threads.

Nightly scheduled runs execute a broader matrix:

- Chrome regression for `@parallel-safe` scenarios.
- Firefox regression for `@parallel-safe` scenarios.
- API regression.
- Stateful regression serial.

Manual workflow dispatch runs exactly one selected browser/suite/parallel combination from the GitHub Actions inputs.
