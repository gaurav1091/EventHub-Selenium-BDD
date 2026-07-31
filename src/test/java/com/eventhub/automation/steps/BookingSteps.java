package com.eventhub.automation.steps;

import com.eventhub.automation.api.ApiAssertions;
import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.factories.TestDataFactory;
import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.pages.BookingsPage;
import com.eventhub.automation.pages.EventDetailPage;
import com.eventhub.automation.pages.EventsPage;
import com.eventhub.automation.support.CleanupService;
import com.eventhub.automation.support.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class BookingSteps {
    private final TestContext context;
    private final EventsPage eventsPage = new EventsPage();
    private final EventDetailPage detailPage = new EventDetailPage();
    private final BookingsPage bookingsPage = new BookingsPage();

    public BookingSteps(TestContext context) {
        this.context = context;
    }

    @When("I open details for event {string}")
    public void iOpenDetailsForEvent(String eventName) {
        eventsPage.openEventsPage();
        eventsPage.bookEvent(eventName);
        detailPage.assertLoaded(eventName);
    }

    @When("I book {int} ticket(s) for event {string}")
    public void iBookTicketsForEvent(int quantity, String eventName) {
        eventsPage.openEventsPage();
        eventsPage.bookEvent(eventName);
        String eventId = context.apiClient().findEventIdByTitle(eventName);
        BookingRequest booking = TestDataFactory.booking(eventId, quantity);
        context.setLastBookingRequest(booking);
        context.put("selectedEventTitle", eventName);
        context.put("selectedEventId", eventId);
        if (quantity > 1) {
            detailPage.increaseTickets(quantity - 1);
        }
        detailPage.fillBookingForm(booking);
        detailPage.confirmBooking();
    }

    @When("I book {int} ticket(s) for a bookable event")
    public void iBookTicketsForABookableEvent(int quantity) {
        String eventTitle = context.apiClient().findBookableEventTitleWithSeatsAtLeast(quantity);
        String eventId = context.apiClient().findEventIdByTitle(eventTitle);
        eventsPage.openEventsPage();
        eventsPage.bookEvent(eventTitle);
        BookingRequest booking = TestDataFactory.booking(eventId, quantity);
        context.setLastBookingRequest(booking);
        context.put("selectedEventId", eventId);
        context.put("selectedEventTitle", eventTitle);
        if (quantity > 1) {
            detailPage.increaseTickets(quantity - 1);
        }
        detailPage.fillBookingForm(booking);
        detailPage.confirmBooking();
    }

    @When("I open details for a bookable event")
    public void iOpenDetailsForABookableEvent() {
        eventsPage.openEventsPage();
        eventsPage.bookFirstAvailableEvent();
    }

    @When("I open details for an event with at least {int} available tickets")
    public void iOpenDetailsForAnEventWithAtLeastAvailableTickets(int minimumTickets) {
        String eventTitle = context.apiClient().findBookableEventTitleWithSeatsAtLeast(minimumTickets);
        eventsPage.openEventsPage();
        eventsPage.openEventByTitle(eventTitle);
        detailPage.assertLoaded(eventTitle);
    }

    @When("I open My Bookings from the confirmation")
    public void iOpenMyBookingsFromTheConfirmation() {
        detailPage.openMyBookingsFromConfirmation();
    }

    @When("I open the booking details")
    public void iOpenTheBookingDetails() {
        bookingsPage.openDetails();
    }

    @When("I cancel the booking")
    public void iCancelTheBooking() {
        bookingsPage.cancelBooking();
    }

    @When("I clear all bookings")
    public void iClearAllBookings() {
        bookingsPage.clearAllBookings();
    }

    @When("I increase tickets by {int}")
    public void iIncreaseTicketsBy(int count) {
        detailPage.increaseTickets(count);
    }

    @When("I decrease tickets by {int}")
    public void iDecreaseTicketsBy(int count) {
        detailPage.decreaseTickets(count);
    }

    @When("I submit the booking form without customer details")
    public void iSubmitTheBookingFormWithoutCustomerDetails() {
        detailPage.submitEmptyBookingForm();
    }

    @When("I enter booking customer email {string} and phone {string}")
    public void iEnterBookingCustomerEmailAndPhone(String email, String phone) {
        String eventId = context.apiClient().findEventIdByTitle("Dilli Diwali Mela");
        BookingRequest booking = new BookingRequest(eventId, "Selenium Invalid", email, phone, 1);
        detailPage.fillBookingForm(booking);
    }

    @When("I enter valid booking customer details for a bookable event")
    public void iEnterValidBookingCustomerDetailsForABookableEvent() {
        eventsPage.openEventsPage();
        eventsPage.bookFirstAvailableEvent();
        String eventId = context.apiClient().findFirstBookableEventId();
        BookingRequest booking = TestDataFactory.booking(eventId, 1);
        context.setLastBookingRequest(booking);
        context.put("selectedEventId", eventId);
        detailPage.fillBookingForm(booking);
    }

    @When("I create a booking through the API for cleanup")
    public void iCreateABookingThroughTheApiForCleanup() {
        String eventId = context.apiClient().findFirstBookableEventId();
        BookingRequest booking = TestDataFactory.booking(eventId, 1);
        context.setLastBookingRequest(booking);
        context.put("selectedEventId", eventId);
        context.put("selectedEventTitle", context.apiClient().findEventTitleById(eventId));
        Response response = context.apiClient().createBooking(booking);
        ApiAssertions.assertSuccess(response);
    }

    @When("I clean Selenium-created bookings through the API")
    public void iCleanSeleniumCreatedBookingsThroughTheApi() {
        new CleanupService(context.apiClient())
                .deleteBookingsByCustomerPrefix(ConfigReader.getRequired("booking.customer.prefix"));
    }

    @Then("I should see the booking confirmation")
    public void iShouldSeeTheBookingConfirmation() {
        detailPage.assertBookingConfirmed();
    }

    @Then("I should see a booking confirmation with total {string}")
    public void iShouldSeeABookingConfirmationWithTotal(String total) {
        detailPage.assertBookingConfirmed();
        detailPage.assertTotalContains(total);
    }

    @Then("the booking confirmation should show the selected event, customer, quantity, and total")
    public void theBookingConfirmationShouldShowTheSelectedEventCustomerQuantityAndTotal() {
        BookingRequest booking = context.lastBookingRequest();
        int unitPrice = context.apiClient().findEventPriceById(context.get("selectedEventId", String.class));
        detailPage.assertBookingConfirmationDetails(
                booking,
                context.get("selectedEventTitle", String.class),
                unitPrice * booking.quantity());
    }

    @Then("I should see booking for event {string}")
    public void iShouldSeeBookingForEvent(String eventName) {
        bookingsPage.assertBookingVisible(eventName);
    }

    @Then("I should see booking for the selected event")
    public void iShouldSeeBookingForTheSelectedEvent() {
        bookingsPage.assertBookingVisible(context.get("selectedEventTitle", String.class));
    }

    @Then("I should see the booking details for event {string}")
    public void iShouldSeeTheBookingDetailsForEvent(String eventName) {
        bookingsPage.assertBookingVisible(eventName);
    }

    @Then("I should not see booking for event {string}")
    public void iShouldNotSeeBookingForEvent(String eventName) {
        bookingsPage.assertBookingNotVisible(eventName);
    }

    @Then("no bookings for the current Selenium customer should remain")
    public void noBookingsForTheCurrentSeleniumCustomerShouldRemain() {
        bookingsPage.openBookingsPage();
        bookingsPage.assertBookingNotVisible(ConfigReader.getRequired("booking.customer.prefix"));
    }

    @Then("My Bookings should show an empty state")
    public void myBookingsShouldShowAnEmptyState() {
        bookingsPage.assertEmptyStateVisible();
    }

    @Then("I should see the event detail booking panel for {string}")
    public void iShouldSeeTheEventDetailBookingPanelFor(String eventName) {
        detailPage.assertLoaded(eventName);
    }

    @Then("I should see the event detail booking panel")
    public void iShouldSeeTheEventDetailBookingPanel() {
        detailPage.assertBookingPanelVisible();
    }

    @Then("the booking form should contain the generated customer details")
    public void theBookingFormShouldContainTheGeneratedCustomerDetails() {
        detailPage.assertBookingFormContains(context.lastBookingRequest());
    }

    @Then("the ticket quantity should be {int}")
    public void theTicketQuantityShouldBe(int quantity) {
        detailPage.assertQuantity(String.valueOf(quantity));
    }

    @Then("the ticket decrement control should be disabled")
    public void theTicketDecrementControlShouldBeDisabled() {
        detailPage.assertDecrementDisabled();
    }

    @Then("the ticket increment control should be disabled")
    public void theTicketIncrementControlShouldBeDisabled() {
        detailPage.assertIncrementDisabled();
    }

    @Then("the booking total should include {string}")
    public void theBookingTotalShouldInclude(String total) {
        detailPage.assertTotalContains(total);
    }

    @Then("the booking form should show required field validation")
    public void theBookingFormShouldShowRequiredFieldValidation() {
        detailPage.assertBookingFormValidation();
    }

    @Then("the booking email field should be invalid")
    public void theBookingEmailFieldShouldBeInvalid() {
        detailPage.assertEmailInvalid();
    }

    @Then("the booking phone field should be invalid")
    public void theBookingPhoneFieldShouldBeInvalid() {
        detailPage.assertPhoneInvalid();
    }
}
