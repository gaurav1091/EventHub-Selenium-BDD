.PHONY: smoke api ui-critical visual accessibility p0 p1 impact parallel quality tag-audit docker-smoke clean-reports

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

p0:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@p0"

p1:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@p1 and @regression"

impact:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dcucumber.filter.tags="@impact-$(AREA)"

parallel:
	mvn test -Dheadless=true -Dbrowser=$(BROWSER) -Dsuite.xml.file=target/test-classes/suites/testng-parallel.xml -Dparallel=methods -Dthread.count=$(THREAD_COUNT) -Dcucumber.filter.tags="(@parallel-safe) and not @stateful"

quality:
	mvn -Pquality -DskipTests verify

tag-audit:
	TAG_AUDIT_FAIL=true bash scripts/audit-tags.sh

docker-smoke:
	SUITE=docker-smoke BROWSER=$(BROWSER) docker compose run --rm eventhub-tests

clean-reports:
	mvn clean
