package com.eventhub.automation.steps;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.eventhub.automation.config.ConfigReader;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
    private static final Path VISUAL_SANITY_DIR = Path.of("target", "visual-sanity");
    private static final Path VISUAL_DIFF_DIR = Path.of("target", "visual-diff");

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
            case "event detail":
                new EventsPage().openEventsPage().bookFirstAvailableEvent();
                Waits.visibleTextContains(DriverManager.getDriver(), "Book Tickets");
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

    @Then("the page should satisfy the configured accessibility threshold")
    public void thePageShouldSatisfyTheConfiguredAccessibilityThreshold() {
        WebDriver driver = DriverManager.getDriver();
        Results results = new AxeBuilder().analyze(driver);
        int violationCount = results.getViolations().size();
        int maxViolations = ConfigReader.getInt("accessibility.max.violations");
        Map<String, Object> report = axeReport(driver, results);
        report.put("mode", thresholdEnabled() ? "threshold" : "advisory-threshold-disabled");
        report.put("maxViolations", maxViolations);
        String json = toJson(report);
        writeAxeReport(driver, json);
        Allure.addAttachment("Axe accessibility threshold", "application/json", json, ".json");
        if (thresholdEnabled()) {
            assertThat(violationCount)
                    .as("Expected Axe violation count for %s to be <= configured threshold",
                            driver.getCurrentUrl())
                    .isLessThanOrEqualTo(maxViolations);
        }
    }

    @Then("the page should have a visual sanity screenshot")
    public void thePageShouldHaveAVisualSanityScreenshot() {
        WebDriver driver = DriverManager.getDriver();
        Waits.pageReady(driver);
        Waits.loadingComplete(driver);
        assertThat(hasVisibleHeadingOrLandmark(driver))
                .as("Expected page to be visually non-blank: %s", driver.getCurrentUrl())
                .isTrue();
        try {
            Files.createDirectories(VISUAL_SANITY_DIR);
            byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver)
                    .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            Path screenshotPath = VISUAL_SANITY_DIR.resolve(visualFileName(driver));
            Files.write(screenshotPath, screenshot);
            assertVisualBaseline(driver, screenshot);
            Allure.addAttachment("Visual sanity screenshot", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write visual sanity screenshot", exception);
        }
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

    private boolean thresholdEnabled() {
        return Boolean.parseBoolean(ConfigReader.getRequired("accessibility.threshold.enabled"));
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

    private String visualFileName(WebDriver driver) {
        String path = driver.getCurrentUrl()
                .replaceFirst("^https?://", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return "visual-" + path + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))
                + ".png";
    }

    private void assertVisualBaseline(WebDriver driver, byte[] screenshot) throws IOException {
        if (!ConfigReader.getBoolean("visual.baseline.enabled")) {
            return;
        }
        Path baselineDir = Path.of(ConfigReader.getRequired("visual.baseline.dir"));
        Path baseline = baselineDir.resolve(stableVisualBaselineName(driver));
        if (ConfigReader.getBoolean("visual.baseline.update")) {
            Files.createDirectories(baselineDir);
            Files.write(baseline, screenshot);
            writeVisualDiffReport(driver, baseline, 0, null, "baseline-updated");
            return;
        }
        if (!Files.exists(baseline)) {
            writeVisualDiffReport(driver, baseline, Long.MAX_VALUE, null, "missing-baseline");
            throw new AssertionError("Missing visual baseline: " + baseline
                    + ". Run with -Dvisual.baseline.update=true after intentional UI changes.");
        }

        byte[] expected = Files.readAllBytes(baseline);
        VisualDiff visualDiff = writeVisualDiffImage(driver, expected, screenshot);
        writeVisualDiffReport(driver, baseline, visualDiff.differentPixels(), visualDiff.diffImage(), "compared");
        Allure.addAttachment("Visual baseline comparison", "application/json",
                Files.newInputStream(VISUAL_DIFF_DIR.resolve(stableVisualBaselineName(driver).replace(".png", ".json"))),
                ".json");
        Allure.addAttachment("Visual baseline diff image", "image/png",
                Files.newInputStream(visualDiff.diffImage()),
                ".png");
        assertThat(visualDiff.differentPixels())
                .as("Expected visual baseline difference for %s to be <= visual.diff.max.pixels",
                        driver.getCurrentUrl())
                .isLessThanOrEqualTo(ConfigReader.getInt("visual.diff.max.pixels"));
    }

    private void writeVisualDiffReport(WebDriver driver, Path baseline, long difference, Path diffImage, String mode)
            throws IOException {
        Files.createDirectories(VISUAL_DIFF_DIR);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("mode", mode);
        report.put("url", driver.getCurrentUrl());
        report.put("baseline", baseline.toString());
        report.put("diffImage", diffImage == null ? "" : diffImage.toString());
        report.put("differentPixels", difference);
        report.put("maxDifferentPixels", ConfigReader.getInt("visual.diff.max.pixels"));
        report.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        Files.writeString(
                VISUAL_DIFF_DIR.resolve(stableVisualBaselineName(driver).replace(".png", ".json")),
                toJson(report),
                StandardCharsets.UTF_8
        );
    }

    private VisualDiff writeVisualDiffImage(WebDriver driver, byte[] expectedBytes, byte[] actualBytes) throws IOException {
        BufferedImage expected = ImageIO.read(new ByteArrayInputStream(expectedBytes));
        BufferedImage actual = ImageIO.read(new ByteArrayInputStream(actualBytes));
        if (expected == null || actual == null) {
            throw new AssertionError("Unable to read visual baseline or actual screenshot as PNG.");
        }

        int width = Math.max(expected.getWidth(), actual.getWidth());
        int height = Math.max(expected.getHeight(), actual.getHeight());
        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        long difference = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean inExpected = x < expected.getWidth() && y < expected.getHeight();
                boolean inActual = x < actual.getWidth() && y < actual.getHeight();
                if (!inExpected || !inActual || expected.getRGB(x, y) != actual.getRGB(x, y)) {
                    difference++;
                    diff.setRGB(x, y, 0xB0FF0000);
                } else {
                    diff.setRGB(x, y, 0x22000000);
                }
            }
        }

        Files.createDirectories(VISUAL_DIFF_DIR);
        Path diffImage = VISUAL_DIFF_DIR.resolve(stableVisualBaselineName(driver).replace("baseline-", "diff-"));
        ImageIO.write(diff, "png", diffImage.toFile());
        return new VisualDiff(diffImage, difference);
    }

    private record VisualDiff(Path diffImage, long differentPixels) {
    }

    private String stableVisualBaselineName(WebDriver driver) {
        String path = driver.getCurrentUrl()
                .replaceFirst("^https?://", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return "baseline-" + path + ".png";
    }

    private String toJson(Map<String, Object> report) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize Axe accessibility advisory report", exception);
        }
    }
}
