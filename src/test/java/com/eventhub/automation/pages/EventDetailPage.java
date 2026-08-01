package com.eventhub.automation.pages;

import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.utils.Waits;
import org.openqa.selenium.By;

import static com.eventhub.automation.utils.UiAssertions.assertPageContains;
import static com.eventhub.automation.utils.UiAssertions.assertPageContainsAnyOf;
import static org.assertj.core.api.Assertions.assertThat;

public class EventDetailPage extends BasePage {
    private static final By PLUS = By.xpath("//button[normalize-space()='+']");
    private static final By MINUS = By.xpath("//button[normalize-space()='−' or normalize-space()='-']");
    private static final By FULL_NAME = By.cssSelector("input[placeholder='Your full name'], input[name*='name' i]");
    private static final By EMAIL = By.cssSelector("input[type='email'], input[placeholder='you@email.com']");
    private static final By PHONE = By.cssSelector("input[placeholder='+91 98765 43210'], input[type='tel'], input[name*='phone' i]");
    private static final By CONFIRM_BOOKING = By.xpath("//button[normalize-space()='Confirm Booking']");
    private static final By BOOK_TICKETS = By.xpath("//*[contains(normalize-space(.),'Book Tickets')]");

    public void assertLoaded(String eventName) {
        assertPageContains(driver(), eventName);
        assertPageContains(driver(), "Book Tickets");
        assertPageContains(driver(), "Confirm Booking");
    }

    public void assertMetadataFor(String eventName) {
        assertPageContains(driver(), eventName);
        assertThat(visible(By.xpath("//*[contains(translate(normalize-space(.),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'seat')]")).getText())
                .containsPattern("\\d+\\s+seats?");
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
        assertPageContains(driver(), quantity);
    }

    public void assertDecrementDisabled() {
        assertThat(visible(MINUS).isEnabled()).isFalse();
    }

    public void assertIncrementDisabled() {
        assertThat(visible(PLUS).isEnabled()).isFalse();
    }

    public void assertTotalContains(String amount) {
        assertPageContains(driver(), amount);
    }

    public void fillBookingForm(BookingRequest booking) {
        type(FULL_NAME, booking.customerName());
        type(EMAIL, booking.customerEmail());
        type(PHONE, booking.customerPhone());
    }

    public void assertBookingFormContains(BookingRequest booking) {
        assertThat(visible(FULL_NAME).getAttribute("value")).isEqualTo(booking.customerName());
        assertThat(visible(EMAIL).getAttribute("value")).isEqualTo(booking.customerEmail());
        assertThat(visible(PHONE).getAttribute("value")).isEqualTo(booking.customerPhone());
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
        String phone = visible(PHONE).getAttribute("value");
        assertThat(phone)
                .as("Expected phone field value to be invalid for EventHub booking rules")
                .doesNotMatch("^\\+?[0-9][0-9\\s-]{7,}$");
    }

    public void assertBookingConfirmed() {
        Waits.visibleTextContains(driver(), "Booking Ref");
        assertPageContains(driver(), "Booking Ref");
        assertPageContains(driver(), "Customer");
    }

    public void assertBookingConfirmationDetails(BookingRequest booking, String eventTitle, int expectedTotal) {
        assertBookingConfirmed();
        assertPageContains(driver(), eventTitle);
        assertPageContains(driver(), booking.customerName());
        assertPageContains(driver(), String.valueOf(booking.quantity()));
        assertPageContainsAnyOf(driver(), "$" + expectedTotal, "$" + String.format("%,d", expectedTotal));
    }

    public void assertBookingPanelVisible() {
        visible(BOOK_TICKETS);
        visible(CONFIRM_BOOKING);
        assertPageContains(driver(), "Book Tickets");
        assertPageContains(driver(), "Confirm Booking");
    }

    public void openMyBookingsFromConfirmation() {
        click(By.xpath("//*[self::button or self::a][contains(normalize-space(.),'View My Bookings')]"));
    }
}
