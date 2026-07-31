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
| `target/governance/tag-audit.json` | Scenario tag compliance from the audit script. |
| `screenshots` | Failure screenshots for the latest run only. |

## Triage Patterns

- `@stateful` failure: rerun serially with `-Dparallel=none`.
- Auth failure: inspect `.env`, `user.email`, `user.password`, and `target/run-summary/environment-health.json`.
- UI-only failure: run headed once and check the final URL in the report metadata.
- API contract failure: compare response body with `src/test/resources/schemas`.
- CI infrastructure failure: check `target/run-logs` for Maven transfer or browser startup errors.
