package com.eventhub.automation.pages;

import com.eventhub.automation.models.EventRequest;
import com.eventhub.automation.utils.Waits;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminEventsPage extends BasePage {
    public AdminEventsPage openAdminEventsPage() {
        open("/admin/events");
        return this;
    }

    public void assertLoaded() {
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains("All Events")
                || webDriver.getPageSource().contains("Add New Event")
                || webDriver.getPageSource().contains("Create Event")
                || webDriver.getPageSource().contains("Manage Events"));
        assertThat(driver().getPageSource()).containsAnyOf("All Events", "Add New Event", "Create Event", "Manage Events");
    }

    public void submitEmptyForm() {
        openCreateFormIfNeeded();
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
        assertThat(driver().getPageSource()).containsAnyOf("required", "Required", "Please fill");
    }

    public void assertEventVisible(String eventName) {
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains(eventName));
        assertThat(driver().getPageSource()).contains(eventName);
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
}
