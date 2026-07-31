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
| Impact | `@impact-auth`, `@impact-events`, `@impact-bookings`, `@impact-api`, `@impact-ux`, `@impact-integration` | Supports faster targeted CI selection. |
| Risk path | `@negative`, `@api-cleanup`, `@retryable` | Marks error-path, cleanup, and controlled retry scenarios. |

Every run writes `target/governance/scenario-governance.json` with scenario names, effective tags, and missing recommended tag groups.

## Coverage Map

| Area | Current automated coverage |
| --- | --- |
| Authentication | Login/logout, invalid credentials, required fields, protected route and admin route redirects, session refresh persistence across protected pages. |
| Events | Listing, title/venue/city search, category/city filtering, clear filters, filter reset after refresh and navigation, empty search result, event detail metadata, deep link metadata. |
| Bookings | Creation, confirmation data accuracy, details, cancel, clear all, empty state after cleanup, invalid email/phone, required customer validation, quantity controls, overbooking prevention. |
| Admin | Admin page rendering, required-field validation, API-created admin event visible in UI, cleanup verification, sold-out event behavior. |
| API Contract | Health, login, current user, events, event detail, bookings, booking list POJO mapping, duplicate booking behavior, capacity boundary, error responses, unauthorized access, invalid payloads, repeated cancellation behavior. |
| Hybrid | API-created booking visible in UI, API-created event searchable/deep-linkable in UI, API-created event visible in admin/discovery, UI booking visible through API, API cleanup reflected in UI, API-created booking details open in UI, API-created sold-out event unavailable in UI. |
| UX Resilience | Responsive smoke, visual sanity screenshots, optional visual baseline comparison, Axe advisory reports, and optional Axe threshold enforcement for key pages. |

## Observability Artifacts

| Artifact | Purpose |
| --- | --- |
| `target/run-summary/eventhub-run-summary.json` | Run metadata, pass/fail/retry totals, and slowest scenario snapshot. |
| `target/run-summary/scenario-durations.json` | Duration and status for each scenario. |
| `target/run-summary/slow-scenarios.json` | Slowest scenarios for runtime triage. |
| `target/run-summary/retry-governance.json` | Retry usage, retry threshold status, and retried scenario metadata. |
| `target/run-summary/impact-selection.json` | Impact-area selection summary for targeted CI and local runs. |
| `target/run-summary/environment-health.json` | Preflight UI/API/credential health result. |
| `target/run-summary/github-step-summary.md` | Markdown summary published in GitHub Actions. |
| `target/run-summary/report-index.html` | One-page local index linking to major reports and artifacts. |
| `target/axe-reports` | Axe accessibility advisory and threshold report data. |
| `target/visual-sanity` | Visual sanity screenshots for key browser pages. |
| `target/visual-diff` | Optional visual baseline comparison reports. |
| `target/governance/scenario-governance.json` | Scenario tag governance and traceability report. |
| `target/governance/test-catalog.md` | Generated scenario-by-scenario catalog with tags and source locations. |
| `target/governance/test-catalog-summary.md` | Generated human summary of coverage by impact and priority. |
| `target/governance/tag-audit.json` | Standalone tag audit output; can fail CI with `TAG_AUDIT_FAIL=true`. |
