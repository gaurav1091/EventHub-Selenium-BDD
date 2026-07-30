package com.eventhub.automation.steps;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.eventhub.automation.drivers.DriverManager;
import com.eventhub.automation.pages.AdminEventsPage;
import com.eventhub.automation.pages.BookingsPage;
import com.eventhub.automation.pages.EventsPage;
import com.eventhub.automation.pages.HomePage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.utils.Waits;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UxSmokeSteps {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path AXE_REPORTS_DIR = Path.of("target", "axe-reports");

    @When("I use viewport {int} by {int}")
    public void iUseViewportBy(int width, int height) {
        DriverManager.getDriver().manage().window().setSize(new Dimension(width, height));
    }

    @When("I open UX smoke page {string}")
    public void iOpenUxSmokePage(String page) {
        switch (page.toLowerCase()) {
            case "home":
                new HomePage().assertLoaded();
                break;
            case "login":
                new LoginPage().openLoginPage().assertLoaded();
                break;
            case "events":
                new EventsPage().openEventsPage();
                break;
            case "bookings":
                new BookingsPage().openBookingsPage().assertLoaded();
                break;
            case "admin events":
                new AdminEventsPage().openAdminEventsPage().assertLoaded();
                break;
            default:
                throw new IllegalArgumentException("Unsupported UX smoke page: " + page);
        }
    }

    @Then("the page should generate an Axe accessibility advisory report")
    public void thePageShouldGenerateAnAxeAccessibilityAdvisoryReport() {
        WebDriver driver = DriverManager.getDriver();
        Results results = new AxeBuilder().analyze(driver);
        Map<String, Object> report = axeReport(driver, results);
        String json = toJson(report);
        writeAxeReport(driver, json);
        Allure.addAttachment("Axe accessibility advisory", "application/json", json, ".json");
        assertThat(results).as("Expected Axe to return an accessibility result").isNotNull();
    }

    @Then("the page should fit the viewport without horizontal overflow")
    public void thePageShouldFitTheViewportWithoutHorizontalOverflow() {
        WebDriver driver = DriverManager.getDriver();
        Waits.pageReady(driver);
        long scrollWidth = ((Number) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.scrollWidth")).longValue();
        long clientWidth = ((Number) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.clientWidth")).longValue();
        assertThat(scrollWidth)
                .as("Expected page at %s not to overflow horizontally", driver.getCurrentUrl())
                .isLessThanOrEqualTo(clientWidth + 2);
    }

    @Then("the page should expose basic accessibility semantics")
    public void thePageShouldExposeBasicAccessibilitySemantics() {
        WebDriver driver = DriverManager.getDriver();
        assertThat(driver.getTitle())
                .as("Expected browser title for %s", driver.getCurrentUrl())
                .isNotBlank();
        assertThat(hasVisibleHeadingOrLandmark(driver))
                .as("Expected a visible heading or main landmark on %s", driver.getCurrentUrl())
                .isTrue();
        if (!driver.getCurrentUrl().contains("/admin/events")) {
            assertVisibleControlsHaveAccessibleNames(driver);
        }
    }

    private boolean hasVisibleHeadingOrLandmark(WebDriver driver) {
        return driver.findElements(By.cssSelector("h1, h2, [role='heading'], main, [role='main']")).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    private void assertVisibleControlsHaveAccessibleNames(WebDriver driver) {
        List<WebElement> controls = driver.findElements(By.cssSelector("button, a, input, select, textarea"));
        for (WebElement control : controls) {
            if (!control.isDisplayed()) {
                continue;
            }
            String accessibleName = String.join(" ",
                    control.getText(),
                    value(control, "aria-label"),
                    value(control, "title"),
                    value(control, "placeholder"),
                    value(control, "name")).trim();
            assertThat(accessibleName)
                    .as("Expected visible control <%s> on %s to have accessible text or naming metadata",
                            control.getTagName(), driver.getCurrentUrl())
                    .isNotBlank();
        }
    }

    private String value(WebElement element, String attribute) {
        String value = element.getAttribute(attribute);
        return value == null ? "" : value;
    }

    private Map<String, Object> axeReport(WebDriver driver, Results results) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("mode", "advisory");
        report.put("url", driver.getCurrentUrl());
        report.put("title", driver.getTitle());
        report.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("violationCount", results.getViolations().size());
        report.put("violations", results.getViolations().stream()
                .map(this::violation)
                .collect(Collectors.toList()));
        return report;
    }

    private Map<String, Object> violation(Rule rule) {
        Map<String, Object> violation = new LinkedHashMap<>();
        violation.put("id", rule.getId());
        violation.put("impact", rule.getImpact());
        violation.put("description", rule.getDescription());
        violation.put("help", rule.getHelp());
        violation.put("helpUrl", rule.getHelpUrl());
        violation.put("tags", rule.getTags());
        violation.put("nodeCount", rule.getNodes().size());
        violation.put("nodes", rule.getNodes().stream()
                .map(node -> {
                    Map<String, Object> nodeReport = new LinkedHashMap<>();
                    nodeReport.put("target", node.getTarget());
                    nodeReport.put("html", node.getHtml());
                    nodeReport.put("failureSummary", node.getFailureSummary());
                    return nodeReport;
                })
                .collect(Collectors.toList()));
        return violation;
    }

    private void writeAxeReport(WebDriver driver, String json) {
        try {
            Files.createDirectories(AXE_REPORTS_DIR);
            Files.writeString(AXE_REPORTS_DIR.resolve(reportFileName(driver)), json, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Axe accessibility advisory report", exception);
        }
    }

    private String reportFileName(WebDriver driver) {
        String path = driver.getCurrentUrl()
                .replaceFirst("^https?://", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return "axe-" + path + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))
                + ".json";
    }

    private String toJson(Map<String, Object> report) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize Axe accessibility advisory report", exception);
        }
    }
}
