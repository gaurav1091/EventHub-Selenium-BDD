package com.eventhub.automation.pages;

import com.eventhub.automation.utils.Waits;
import com.eventhub.automation.utils.XpathUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;

import static org.assertj.core.api.Assertions.assertThat;
import static com.eventhub.automation.utils.UiAssertions.assertPageContains;

public class BookingsPage extends BasePage {
    public BookingsPage openBookingsPage() {
        open("/bookings");
        return this;
    }

    public void assertLoaded() {
        Waits.loadingComplete(driver());
        assertPageContains(driver(), "My Bookings");
    }

    public void assertBookingVisible(String eventName) {
        assertThat(eventName).as("expected booking text").isNotBlank();
        Waits.loadingComplete(driver());
        assertPageContains(driver(), "My Bookings");
        assertPageContains(driver(), eventName);
    }

    public void assertBookingNotVisible(String eventName) {
        assertLoaded();
        assertThat(driver().findElements(By.xpath("//*[contains(normalize-space(.),"
                        + XpathUtils.literal(eventName) + ")]")))
                .as("Expected booking text to be absent: %s", eventName)
                .isEmpty();
    }

    public void openDetails() {
        assertLoaded();
        click(By.xpath("//button[contains(normalize-space(.),'Details') or contains(normalize-space(.),'View')]"));
    }

    public void cancelBooking() {
        assertLoaded();
        click(By.xpath("//button[contains(normalize-space(.),'Cancel Booking') or contains(normalize-space(.),'Cancel')]"));
        acceptNativeConfirmIfPresent();
        if (!driver().findElements(By.xpath("//button[normalize-space()='Confirm' or normalize-space()='Yes']")).isEmpty()) {
            click(By.xpath("//button[normalize-space()='Confirm' or normalize-space()='Yes']"));
        }
    }

    public void clearAllBookings() {
        assertLoaded();
        click(By.xpath("//button[contains(normalize-space(.),'Clear all') or contains(normalize-space(.),'Clear All')]"));
        acceptNativeConfirmIfPresent();
        if (!driver().findElements(By.xpath("//button[normalize-space()='Confirm' or normalize-space()='Yes']")).isEmpty()) {
            click(By.xpath("//button[normalize-space()='Confirm' or normalize-space()='Yes']"));
        }
    }

    private void acceptNativeConfirmIfPresent() {
        try {
            driver().switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {
            // Some builds use an in-page confirmation modal instead of window.confirm.
        }
    }
}
