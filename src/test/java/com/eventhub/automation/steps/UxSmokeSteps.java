package com.eventhub.automation.steps;

import com.eventhub.automation.drivers.DriverManager;
import com.eventhub.automation.pages.AdminEventsPage;
import com.eventhub.automation.pages.BookingsPage;
import com.eventhub.automation.pages.EventsPage;
import com.eventhub.automation.pages.HomePage;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.utils.Waits;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UxSmokeSteps {
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
}
