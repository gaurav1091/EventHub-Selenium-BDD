# Contribution Guide

Use this guide when adding framework code, step definitions, page objects, API clients, or scenarios.

## Scenario Rules

Every scenario needs these traceability groups:

| Group | Required pattern |
| --- | --- |
| Priority | `@p0`, `@p1`, `@p2`, or `@p3` |
| Suite | `@smoke`, `@regression`, `@contract`, `@accessibility`, or `@responsive` |
| Surface | `@ui`, `@api`, `@hybrid`, `@accessibility`, or `@responsive` |
| Data safety | `@parallel-safe` or `@stateful` |
| Owner | `@owner-*` |
| Risk | `@risk-*` |
| Intent | `@intent-*` |

Run this before pushing:

```bash
make tag-audit
make quality
```

## Code Rules

- Keep browser behavior in page objects.
- Keep API calls in API clients and API assertions.
- Keep Cucumber steps business-readable and thin.
- Create dynamic data through `TestDataFactory`.
- Clean stateful API/UI data through cleanup hooks or `CleanupService`.
- Do not tag a scenario `@parallel-safe` unless it avoids shared mutable server data.

## Pull Request Checklist

- Added/updated feature tags.
- Added assertions at the right layer.
- Added cleanup for stateful data.
- Ran focused tests and quality gates.
- Updated `docs/test-catalog.md` when business coverage changed.
