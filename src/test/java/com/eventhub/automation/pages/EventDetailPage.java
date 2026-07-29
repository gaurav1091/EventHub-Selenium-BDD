package com.eventhub.automation.pages;

import com.eventhub.automation.models.BookingRequest;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

public class EventDetailPage extends BasePage {
    private static final By PLUS = By.xpath("//button[normalize-space()='+']");
    private static final By MINUS = By.xpath("//button[normalize-space()='−' or normalize-space()='-']");
    private static final By FULL_NAME = By.cssSelector("input[placeholder='Your full name'], input[name*='name' i]");
    private static final By EMAIL = By.cssSelector("input[type='email'], input[placeholder='you@email.com']");
    private static final By PHONE = By.cssSelector("input[placeholder='+91 98765 43210'], input[type='tel'], input[name*='phone' i]");
    private static final By CONFIRM_BOOKING = By.xpath("//button[normalize-space()='Confirm Booking']");

    public void assertLoaded(String eventName) {
        com.eventhub.automation.utils.Waits.until(driver(), webDriver -> webDriver.getPageSource().contains(eventName));
        assertThat(driver().getPageSource()).contains(eventName);
        assertThat(driver().getPageSource()).contains("Book Tickets", "Confirm Booking");
    }

    public void assertMetadataFor(String eventName) {
        com.eventhub.automation.utils.Waits.until(driver(), webDriver -> webDriver.getPageSource().contains(eventName));
        assertThat(driver().getPageSource()).contains(eventName);
        assertThat(driver().getPageSource()).containsPattern("\\d+\\s+seats");
    }

    public void increaseTickets(int times) {
        for (int index = 0; index < times; index++) {
            click(PLUS);
        }
    }

    public void decreaseTickets(int times) {
        for (int index = 0; index < times; index++) {
            click(MINUS);
        }
    }

    public void assertQuantity(String quantity) {
        assertThat(driver().getPageSource()).contains(quantity);
    }

    public void assertDecrementDisabled() {
        assertThat(visible(MINUS).isEnabled()).isFalse();
    }

    public void assertTotalContains(String amount) {
        assertThat(driver().getPageSource()).contains(amount);
    }

    public void fillBookingForm(BookingRequest booking) {
        type(FULL_NAME, booking.customerName());
        type(EMAIL, booking.customerEmail());
        type(PHONE, booking.customerPhone());
    }

    public void confirmBooking() {
        click(CONFIRM_BOOKING);
    }

    public void submitEmptyBookingForm() {
        click(CONFIRM_BOOKING);
    }

    public void assertBookingFormValidation() {
        assertThat(visible(FULL_NAME).getAttribute("validationMessage")).isNotBlank();
        assertThat(visible(EMAIL).getAttribute("validationMessage")).isNotBlank();
    }

    public void assertEmailInvalid() {
        assertThat(visible(EMAIL).getAttribute("validationMessage")).isNotBlank();
    }

    public void assertPhoneInvalid() {
        assertThat(driver().getPageSource()).containsIgnoringCase("valid");
    }

    public void assertBookingConfirmed() {
        com.eventhub.automation.utils.Waits.until(driver(), webDriver -> {
            String source = webDriver.getPageSource();
            return source.contains("Booking Confirmed") || source.contains("Booking Ref");
        });
        assertThat(driver().getPageSource()).contains("Booking Confirmed", "Booking Ref", "Customer");
    }

    public void assertBookingPanelVisible() {
        com.eventhub.automation.utils.Waits.until(driver(), webDriver -> {
            String source = webDriver.getPageSource();
            return source.contains("Book Tickets") && source.contains("Confirm Booking");
        });
        assertThat(driver().getPageSource()).contains("Book Tickets", "Confirm Booking");
    }

    public void openMyBookingsFromConfirmation() {
        click(By.xpath("//*[self::button or self::a][contains(normalize-space(.),'View My Bookings')]"));
    }
}
