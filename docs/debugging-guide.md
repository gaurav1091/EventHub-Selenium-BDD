# Debugging Guide

## Fast Local Reproduction

```bash
mvn test -Dheadless=false -Dbrowser=chrome -Dcucumber.filter.tags="@scenario-tag"
mvn test -Dheadless=true -Dbrowser=firefox -Dcucumber.filter.tags="@api"
```

For parallel-only behavior:

```bash
make parallel BROWSER=chrome THREAD_COUNT=2
```

## Useful Artifacts

| Artifact | Use |
| --- | --- |
| `target/extent-report/EventHub-Cucumber-Report.html` | Human-readable execution report. |
| `target/allure-results` | Allure result data and attachments. |
| `target/run-summary/eventhub-run-summary.json` | Browser, tags, retries, counts, and slowest scenarios. |
| `target/run-summary/environment-health.json` | UI/API/credential preflight status. |
| `target/run-summary/retry-governance.json` | Retry count, threshold status, and retried scenario metadata. |
| `target/run-summary/release-readiness.json` | Consolidated quality status for tests, retries, accessibility, visual baselines, and quarantine. |
| `target/run-summary/accessibility-summary.json` | Axe violation totals by page. |
| `target/run-summary/visual-quality-summary.json` | Visual baseline comparison totals and missing baseline count. |
| `target/run-summary/quarantine-dashboard.md` | Human-readable quarantine dashboard. |
| `target/run-summary/impact-selection.json` | Impact-area selection summary. |
| `target/run-summary/report-index.html` | One-page report navigation index. |
| `target/governance/tag-audit.json` | Scenario tag compliance from the audit script. |
| `target/governance/test-catalog.md` | Generated scenario-level traceability catalog. |
| `target/governance/test-catalog-summary.md` | Generated coverage summary by priority and impact. |
| `target/visual-diff` | Optional visual baseline PNG diff images and JSON comparison reports. |
| `screenshots` | Failure screenshots for the latest run only. |

## Triage Patterns

- `@stateful` failure: rerun serially with `-Dparallel=none`.
- Auth failure: inspect `.env`, `user.email`, `user.password`, and `target/run-summary/environment-health.json`.
- UI-only failure: run headed once and check the final URL in the report metadata.
- API contract failure: compare response body with `src/test/resources/schemas`.
- CI infrastructure failure: check `target/run-logs` for Maven transfer or browser startup errors.
- Retry governance failure: inspect `target/run-summary/retry-governance.json` and remove retries from deterministic failures.
- Release readiness failure: inspect `target/run-summary/release-readiness.md` first, then drill into the failing check-specific JSON artifact.
- Quarantine failure: inspect `target/run-summary/quarantine-dashboard.md` and update `src/test/resources/governance/quarantine.json` with owner, reason, issue, and future expiry.
- Accessibility gate failure: inspect `target/run-summary/accessibility-summary.json` and the page-level JSON files under `target/axe-reports`.
- Visual baseline failure: inspect PNG diff images and JSON reports in `target/visual-diff`; refresh baselines only after intentional UI changes.

## Published Report Links

GitHub Pages publishes each run under:

```text
https://gaurav1091.github.io/EventHub-Selenium-BDD/runs/<run-id>-attempt-<attempt>/
```

Use the run page for browser/suite-specific reports and the root dashboard for historical reports.
Use `trend-dashboard.html` from the root dashboard to compare totals, retries, accessibility findings, visual diffs, and
missing baselines across CI runs.
