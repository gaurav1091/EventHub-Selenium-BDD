package com.eventhub.automation.pages;

import com.eventhub.automation.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingsPage extends BasePage {
    public BookingsPage openBookingsPage() {
        open("/bookings");
        return this;
    }

    public void assertBookingVisible(String eventName) {
        assertThat(eventName).as("expected booking text").isNotBlank();
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains("My Bookings")
                && !webDriver.getPageSource().contains("animate-pulse")
                && !webDriver.getPageSource().contains("animate-spin")
                && webDriver.getPageSource().contains(eventName));
        assertThat(driver().getPageSource()).contains(eventName);
    }

    public void assertBookingNotVisible(String eventName) {
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains("My Bookings")
                && !webDriver.getPageSource().contains("animate-pulse")
                && !webDriver.getPageSource().contains("animate-spin"));
        assertThat(driver().getPageSource()).doesNotContain(eventName);
    }

    public void openDetails() {
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains("My Bookings")
                && !webDriver.getPageSource().contains("animate-pulse")
                && !webDriver.getPageSource().contains("animate-spin"));
        click(By.xpath("//button[contains(normalize-space(.),'Details') or contains(normalize-space(.),'View')]"));
    }

    public void cancelBooking() {
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains("My Bookings")
                && !webDriver.getPageSource().contains("animate-pulse")
                && !webDriver.getPageSource().contains("animate-spin"));
        click(By.xpath("//button[contains(normalize-space(.),'Cancel Booking') or contains(normalize-space(.),'Cancel')]"));
        acceptNativeConfirmIfPresent();
        if (!driver().findElements(By.xpath("//button[normalize-space()='Confirm' or normalize-space()='Yes']")).isEmpty()) {
            click(By.xpath("//button[normalize-space()='Confirm' or normalize-space()='Yes']"));
        }
    }

    public void clearAllBookings() {
        Waits.until(driver(), webDriver -> webDriver.getPageSource().contains("My Bookings")
                && !webDriver.getPageSource().contains("animate-pulse")
                && !webDriver.getPageSource().contains("animate-spin"));
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
