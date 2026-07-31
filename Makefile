.PHONY: smoke api ui-critical visual accessibility accessibility-strict visual-baseline p0 p1 impact impact-select parallel quality governance-check tag-audit docker-smoke clean-reports

BROWSER ?= chrome
THREAD_COUNT ?= 2
AREA ?= auth

smoke:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@smoke"

api:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@api"

ui-critical:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@ui and @critical"

visual:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@visual"

accessibility:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@accessibility"

accessibility-strict:
	mvn test -Paccessibility-strict -Dbrowser=$(BROWSER)

visual-baseline:
	mvn test -Pvisual-baseline -Dbrowser=$(BROWSER)

p0:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@p0"

p1:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@p1 and @regression"

impact:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@impact-$(AREA)"

impact-select:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="$$(bash scripts/select-impact-tags.sh --area $(AREA))"

parallel:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dsuite.xml.file=target/test-classes/suites/testng-parallel.xml -Dparallel=methods -Dthread.count=$(THREAD_COUNT) -Dcucumber.filter.tags="(@parallel-safe) and not @stateful"

quality:
	mvn -Pquality -DskipTests verify

governance-check:
	bash scripts/governance-check.sh

tag-audit:
	TAG_AUDIT_FAIL=true bash scripts/audit-tags.sh

docker-smoke:
	SUITE=docker-smoke BROWSER=$(BROWSER) docker compose run --rm eventhub-tests

clean-reports:
	mvn clean
