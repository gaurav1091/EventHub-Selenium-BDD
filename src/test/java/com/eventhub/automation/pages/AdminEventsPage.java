package com.eventhub.automation.pages;

import com.eventhub.automation.models.EventRequest;
import com.eventhub.automation.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.eventhub.automation.utils.UiAssertions.assertPageContains;
import static com.eventhub.automation.utils.UiAssertions.assertPageContainsAnyOf;
import static org.assertj.core.api.Assertions.assertThat;

public class AdminEventsPage extends BasePage {
    public AdminEventsPage openAdminEventsPage() {
        open("/admin/events");
        return this;
    }

    public void assertLoaded() {
        Waits.loadingComplete(driver());
        assertPageContainsAnyOf(driver(), "All Events", "Add New Event", "Create Event", "Manage Events");
    }

    public void submitEmptyForm() {
        openCreateFormIfNeeded();
        click(By.xpath("//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Save') or contains(normalize-space(.),'Add Event')]"));
    }

    public void submitInvalidNumericValues() {
        openCreateFormIfNeeded();
        type(titleInput(), "Invalid Selenium Event");
        typeIfPresent(descriptionInput(), "Negative test event payload from UI.");
        typeIfPresent(cityInput(), "Delhi");
        typeIfPresent(venueInput(), "Invalid Venue");
        typeIfPresent(dateTimeInput(), "2030-12-31T19:00");
        type(priceInput(), "-1");
        type(seatsInput(), "0");
        click(By.xpath("//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Save') or contains(normalize-space(.),'Add Event')]"));
    }

    public void createEvent(EventRequest event) {
        openCreateFormIfNeeded();
        type(By.cssSelector("input[name='title'], input[placeholder*='title' i]"), event.title());
        type(By.cssSelector("textarea[name='description'], textarea[placeholder*='description' i], input[name='description'], input[placeholder*='description' i]"), event.description());
        selectByTextContaining(By.cssSelector("select[name='category'], select:nth-of-type(1)"), event.category());
        type(By.cssSelector("input[name='city'], input[placeholder*='city' i]"), event.city());
        type(By.cssSelector("input[name='venue'], input[placeholder*='venue' i]"), event.venue());
        type(By.cssSelector("input[type='datetime-local'], input[name*='date' i]"), event.dateTime());
        type(By.cssSelector("input[name='price'], input[placeholder*='price' i]"), String.valueOf(event.price()));
        type(By.cssSelector("input[name='totalSeats'], input[placeholder*='seat' i]"), String.valueOf(event.totalSeats()));
        type(By.cssSelector("input[name='imageUrl'], input[placeholder*='image' i]"), event.imageUrl());
        click(By.xpath("//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Save')]"));
    }

    public void assertValidationVisible() {
        assertPageContainsAnyOf(driver(), "required", "Required", "Please fill");
    }

    public void assertNumericValidationVisible() {
        List<WebElement> numericFields = List.of(visible(priceInput()), visible(seatsInput()));
        boolean browserValidationVisible = numericFields.stream()
                .map(field -> field.getAttribute("validationMessage"))
                .anyMatch(message -> message != null && !message.isBlank());
        boolean inlineValidationVisible = !driver().findElements(By.xpath(
                        "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'price') "
                                + "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'seat') "
                                + "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid') "
                                + "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'greater')]"))
                .isEmpty();
        assertThat(browserValidationVisible || inlineValidationVisible)
                .as("Expected invalid price/seats to trigger browser or inline validation")
                .isTrue();
    }

    public void assertEventVisible(String eventName) {
        assertPageContains(driver(), eventName);
    }

    private void openCreateFormIfNeeded() {
        if (hasElements(By.cssSelector("input[name='title'], input[placeholder*='title' i]"))) {
            return;
        }
        By addNewEvent = By.xpath("//*[self::button or self::a][contains(normalize-space(.),'Add New Event') or contains(normalize-space(.),'Create Event')]");
        if (hasElements(addNewEvent)) {
            click(addNewEvent);
        }
    }

    private By priceInput() {
        return fieldAfterLabel("price");
    }

    private By seatsInput() {
        return fieldAfterLabel("total seats");
    }

    private By titleInput() {
        return fieldAfterLabel("title");
    }

    private By cityInput() {
        return fieldAfterLabel("city");
    }

    private By venueInput() {
        return fieldAfterLabel("venue");
    }

    private By dateTimeInput() {
        return fieldAfterLabel("event date");
    }

    private By descriptionInput() {
        return fieldAfterLabel("description");
    }

    private By fieldAfterLabel(String labelText) {
        String normalized = labelText.toLowerCase();
        return By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + normalized + "')]/following::*[self::input or self::textarea or self::select][1]");
    }

    private void typeIfPresent(By locator, String value) {
        if (!elements(locator).isEmpty()) {
            type(locator, value);
        }
    }
}
