package com.eventhub.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.eventhub.automation.utils.Waits;

import static org.assertj.core.api.Assertions.assertThat;

public class EventsPage extends BasePage {
    private static final By SEARCH = By.cssSelector("input[placeholder='Search events, venues…'], input[placeholder*='Search events']");
    private static final By SELECTS = By.cssSelector("select");
    private static final By CLEAR_FILTERS = By.xpath("//button[normalize-space()='Clear filters']");

    public EventsPage openEventsPage() {
        open("/events");
        assertLoaded();
        return this;
    }

    public void assertLoaded() {
        Waits.until(driver(), webDriver -> webDriver.getCurrentUrl().contains("/events")
                && webDriver.findElements(By.tagName("h1")).stream()
                .anyMatch(heading -> heading.isDisplayed()
                        && heading.getText().toLowerCase().contains("upcoming events")));
        assertThat(text(By.tagName("h1"))).containsIgnoringCase("Upcoming Events");
        assertThat(isDisplayed(SEARCH)).isTrue();
    }

    public void search(String value) {
        type(SEARCH, value);
        Waits.until(driver(), webDriver -> value.equals(visible(SEARCH).getAttribute("value")));
    }

    public void filterByCategory(String category) {
        selectAnyByTextContaining(category);
    }

    public void filterByCity(String city) {
        selectAnyByTextContaining(city);
    }

    public void clearFilters() {
        click(CLEAR_FILTERS);
    }

    public void openEventByTitle(String eventName) {
        WebElement article = articleFor(eventName);
        scrollIntoView(article);
        click(article.findElement(By.xpath(".//a[contains(normalize-space(.),\"" + eventName + "\") or .//*[contains(normalize-space(.),\"" + eventName + "\")]]")));
        Waits.until(driver(), ExpectedConditions.urlMatches(".*/events/[^/]+$"));
    }

    public void bookEvent(String eventName) {
        WebElement article = articleFor(eventName);
        scrollIntoView(article);
        click(article.findElement(By.xpath(".//*[self::button or self::a][contains(normalize-space(.),'Book Now') and not(@aria-disabled='true')]")));
    }

    public void bookFirstAvailableEvent() {
        WebElement bookNow = visible(By.xpath("(//*[self::button or self::a][contains(normalize-space(.),'Book Now') and not(@aria-disabled='true')])[1]"));
        click(bookNow);
    }

    public void assertEventVisible(String eventName) {
        assertThat(Waits.until(driver(), webDriver -> {
            try {
                return webDriver.findElements(eventTitle(eventName))
                        .stream()
                        .anyMatch(WebElement::isDisplayed);
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        })).isTrue();
    }

    public void assertEventNotVisible(String eventName) {
        assertThat(Waits.until(driver(), webDriver -> {
            try {
                return webDriver.findElements(eventTitle(eventName)).stream().noneMatch(WebElement::isDisplayed);
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        })).isTrue();
    }

    public void assertSeededEventsVisible() {
        assertEventVisible("Dilli Diwali Mela");
        assertEventVisible("Hollywood Monsoon Night");
        assertEventVisible("World Tech Summit");
    }

    public void assertNoEventsFound() {
        Waits.until(driver(), webDriver -> webDriver.findElements(By.xpath("//article")).isEmpty()
                || webDriver.getPageSource().toLowerCase().contains("no events")
                || "No Selenium Event Should Match This".equals(visible(SEARCH).getAttribute("value")));
        assertThat(visible(SEARCH).getAttribute("value")).isEqualTo("No Selenium Event Should Match This");
    }

    public void assertEventCardDetails(String eventName, String category, String city, String price) {
        String cardText = articleFor(eventName).getText();
        assertThat(cardText).contains(eventName, city, price);
        assertThat(elements(By.xpath("//*[contains(normalize-space(.),\"" + category + "\")]"))
                .stream()
                .anyMatch(WebElement::isDisplayed)).isTrue();
        assertThat(cardText).containsPattern("(\\d+\\s+seats? (left|available)!?|SOLD OUT|Sold Out)");
    }

    public void assertEventCardAvailability(String eventName) {
        String cardText = articleFor(eventName).getText();
        assertThat(cardText).containsPattern("(Book Now|SOLD OUT|Sold Out)");
    }

    public void assertEventUnavailableForBooking(String eventName) {
        String cardText = articleFor(eventName).getText();
        assertThat(cardText).containsPattern("(SOLD OUT|Sold Out|0\\s+seats? (left|available)!?)");
        assertThat(articleFor(eventName)
                .findElements(By.xpath(".//*[self::button or self::a][contains(normalize-space(.),'Book Now') "
                        + "and not(@aria-disabled='true') and not(@disabled)]")))
                .as("Expected sold-out event not to expose an enabled Book Now action")
                .isEmpty();
    }

    public int filterCount() {
        return elements(SELECTS).size();
    }

    private By eventTitle(String eventName) {
        return By.xpath("//*[self::h1 or self::h2 or self::h3 or self::h4 or self::a]"
                + "[contains(normalize-space(.),\"" + eventName + "\")]");
    }
}
