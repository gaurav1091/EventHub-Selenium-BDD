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
| Risk path | `@negative`, `@api-cleanup`, `@retryable` | Marks error-path, cleanup, and controlled retry scenarios. |

Every run writes `target/governance/scenario-governance.json` with scenario names, effective tags, and missing recommended tag groups.

## Coverage Map

| Area | Current automated coverage |
| --- | --- |
| Authentication | Login/logout, invalid credentials, required fields, protected route redirect, session refresh persistence. |
| Events | Listing, title/venue/city search, category/city filtering, clear filters, empty search result, event detail metadata. |
| Bookings | Creation, confirmation, details, cancel, clear all, invalid email/phone, required customer validation, quantity controls, overbooking prevention. |
| Admin | Admin page rendering, required-field validation, API-created admin event visible in UI, sold-out event behavior. |
| API Contract | Health, login, current user, events, event detail, bookings, error responses, unauthorized access, invalid payloads, repeated cancellation behavior. |
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
