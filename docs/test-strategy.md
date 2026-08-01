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
- `@visual`: visual sanity smoke for key screens.
- `@p0`, `@p1`, `@p2`, `@p3`: priority from release-blocking to low-risk supporting coverage.
- `@owner-*`: accountable team or module owner, for example `@owner-platform`.
- `@risk-*`: business or technical risk area, for example `@risk-auth`.
- `@intent-*`: why the scenario exists, for example `@intent-security`.
- `@impact-*`: impact-selection tag for faster targeted CI, for example `@impact-auth`.

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
- Use `@retryable` sparingly for scenarios that have a documented transient infrastructure/UI-rendering failure mode.

Parallel-state tags:

- Use `@parallel-safe` only when the scenario can run at the same time as another scenario without relying on shared mutable server data.
- Use `@stateful` when the scenario creates, updates, cancels, or clears bookings/events/users on the shared EventHub environment.
- Do not combine `@stateful` and `@parallel-safe` unless the data is isolated by run ID and the cleanup path is proven independent.

Coverage governance:

- Keep every scenario covered by a suite tag, surface tag, domain tag, data-safety tag, priority tag, owner tag, risk tag, and intent tag.
- Use `docs/test-catalog.md` as the human-readable coverage map for business and risk traceability.
- Review `target/governance/scenario-governance.json` after local or CI runs when adding broad new coverage.
- Review `target/governance/test-catalog.md` for the generated scenario-by-scenario catalog.
- Run `TAG_AUDIT_FAIL=true bash scripts/audit-tags.sh` or `make tag-audit` before pushing tag changes.

## Parallel Policy

Parallel runs should execute `@parallel-safe` scenarios, or should exclude `@stateful`.

The GitHub Actions workflow and Docker entrypoint automatically add `not @stateful` when `parallel=true`.
This keeps booking/admin data mutation tests serial unless a scenario is explicitly redesigned to be isolated.
Manual or Docker runs fail fast when `parallel=true` is combined with `hybrid`, `stateful`, or `nightly-stateful`, because those suites currently depend on stateful shared-data coverage.
Hybrid coverage intentionally stays serial because it validates API-created or UI-created state across layers before cleanup.
CI and Docker also run `scripts/assert-scenarios-executed.sh` after Maven to fail runs that accidentally match zero scenarios.

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
SUITE=ui-critical docker compose run --rm eventhub-tests
SUITE=hybrid PARALLEL=false docker compose run --rm eventhub-tests
SUITE=impact-auth docker compose run --rm eventhub-tests
IMPACT_AREA=api docker compose run --rm eventhub-tests
```

Manual GitHub Actions dispatch supports these choices:

- Browser: `chrome`, `firefox`
- Suite: `smoke`, `ui`, `api`, `regression`, `admin`, `auth`, `bookings`, `events`, `navigation`, `hybrid`, `stateful`, `parallel-safe`, `p0-smoke`, `p1-regression`, `api-contract`, `ui-critical`, `nightly-stateful`, `accessibility`, `visual`, `impact-auth`, `impact-events`, `impact-bookings`, `impact-admin`, `impact-api`, `impact-ux`, `docker-smoke`, `all`
- Impact area override: `none`, `auth`, `events`, `bookings`, `admin`, `api`, `ux`, `integration`
- Parallel: `true`, `false`
- Thread count: `2`, `3`, `4`, `5`

## Data Policy

Reusable expected data lives in `src/test/resources/test-data/eventhub-test-data.json`.
Dynamic stateful records should be created through factories with unique names and cleaned through API helpers.
Generated stateful test data includes the run ID from `run.id` when provided, or an automatic timestamp-based run ID.

## CI Policy

Push runs execute a fast matrix:

- Chrome smoke, serial.
- Firefox smoke, serial.
- Chrome API, parallel.
- Chrome `@parallel-safe`, parallel with four threads.

Pull requests run PR-diff-aware impact selection instead of the fixed push matrix. Changed files are mapped to `@impact-*` tags through `scripts/select-impact-tags.sh`, and the generated selection is published under `target/run-summary`.

Nightly scheduled runs execute a broader matrix:

- Chrome regression for `@parallel-safe` scenarios.
- Firefox regression for `@parallel-safe` scenarios.
- API regression.
- Stateful regression serial.

Manual workflow dispatch runs exactly one selected browser/suite/parallel combination from the GitHub Actions inputs.

## Suite Aliases

| Suite alias | Cucumber expression |
| --- | --- |
| `p0-smoke` | `@p0 and @smoke` |
| `p1-regression` | `@p1 and @regression` |
| `api-contract` | `@api and @contract` |
| `ui-critical` | `@ui and @critical` |
| `nightly-stateful` | `@stateful and @regression` |
| `accessibility` | `@accessibility` |
| `visual` | `@visual` |
| `impact-*` | matching `@impact-*` tag |
| `docker-smoke` | `@docker-smoke or (@p0 and @smoke)` |

`scripts/select-impact-tags.sh` can generate a targeted impact expression explicitly by area or infer it from changed files when a base ref is available. It writes `target/run-summary/impact-selection.json` and `target/run-summary/impact-selection.md` for local and CI triage.

## Accessibility Thresholds

Axe accessibility scenarios are advisory by default. Enable threshold enforcement when the product baseline is ready:

```bash
mvn test -Dheadless=true -Dcucumber.filter.tags="@accessibility" -Daccessibility.threshold.enabled=true -Daccessibility.max.violations=0
mvn -Paccessibility-strict test
```

## Visual Baselines

Visual sanity screenshots are always generated for `@visual` scenarios. Baseline comparison is optional and disabled by default:

```bash
mvn -Pvisual-baseline test
mvn -Pvisual-baseline test -Dvisual.baseline.update=true
```

PNG diff images and JSON comparison reports are written under `target/visual-diff`. Keep this advisory until the UI has stable, committed baselines. Use `-Dvisual.diff.max.pixels=<count>` to set the enforcement tolerance.
