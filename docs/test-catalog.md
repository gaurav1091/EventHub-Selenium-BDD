# EventHub Test Catalog

This catalog maps automated scenarios to business risk areas and execution intent.

## Governance Tags

Use these tag groups when adding or reviewing scenarios:

| Tag group | Examples | Purpose |
| --- | --- | --- |
| Suite | `@smoke`, `@regression` | Controls fast confidence vs deeper coverage. |
| Surface | `@ui`, `@api`, `@hybrid`, `@accessibility`, `@responsive` | Identifies the automation layer. |
| Domain | `@auth`, `@events`, `@bookings`, `@admin`, `@navigation` | Maps tests to product area. |
| Data safety | `@parallel-safe`, `@stateful` | Separates isolated tests from shared-state tests. |
| Priority | `@p0`, `@p1`, `@p2`, `@p3` | Communicates release impact and triage order. |
| Owner | `@owner-platform` | Identifies accountable team/module. |
| Risk area | `@risk-auth`, `@risk-revenue`, `@risk-contract`, `@risk-ux` | Maps scenarios to business/technical risk. |
| Intent | `@intent-security`, `@intent-booking`, `@intent-discovery` | Captures why the scenario exists. |
| Risk path | `@negative`, `@api-cleanup`, `@retryable` | Marks error-path, cleanup, and controlled retry scenarios. |

Every run writes `target/governance/scenario-governance.json` with scenario names, effective tags, and missing recommended tag groups.

## Coverage Map

| Area | Current automated coverage |
| --- | --- |
| Authentication | Login/logout, invalid credentials, required fields, protected route and admin route redirects, session refresh persistence. |
| Events | Listing, title/venue/city search, category/city filtering, clear filters, filter reset after refresh, empty search result, event detail metadata, deep link metadata. |
| Bookings | Creation, confirmation data accuracy, details, cancel, clear all, empty state after cleanup, invalid email/phone, required customer validation, quantity controls, overbooking prevention. |
| Admin | Admin page rendering, required-field validation, API-created admin event visible in UI, cleanup verification, sold-out event behavior. |
| API Contract | Health, login, current user, events, event detail, bookings, booking list POJO mapping, duplicate booking behavior, capacity boundary, error responses, unauthorized access, invalid payloads, repeated cancellation behavior. |
| UX Resilience | Responsive smoke for key authenticated pages and Axe accessibility advisory reports. |

## Observability Artifacts

| Artifact | Purpose |
| --- | --- |
| `target/run-summary/eventhub-run-summary.json` | Run metadata, pass/fail/retry totals, and slowest scenario snapshot. |
| `target/run-summary/scenario-durations.json` | Duration and status for each scenario. |
| `target/run-summary/slow-scenarios.json` | Slowest scenarios for runtime triage. |
| `target/run-summary/environment-health.json` | Preflight UI/API/credential health result. |
| `target/run-summary/github-step-summary.md` | Markdown summary published in GitHub Actions. |
| `target/governance/scenario-governance.json` | Scenario tag governance and traceability report. |
| `target/governance/tag-audit.json` | Standalone tag audit output; can fail CI with `TAG_AUDIT_FAIL=true`. |
