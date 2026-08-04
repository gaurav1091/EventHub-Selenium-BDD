package com.eventhub.automation.steps;

import com.eventhub.automation.api.ApiAssertions;
import com.eventhub.automation.factories.TestDataFactory;
import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.models.EventRequest;
import com.eventhub.automation.pages.AdminEventsPage;
import com.eventhub.automation.pages.EventDetailPage;
import com.eventhub.automation.pages.EventsPage;
import com.eventhub.automation.support.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class AdminSteps {
    private final TestContext context;
    private final AdminEventsPage adminEventsPage = new AdminEventsPage();
    private final EventsPage eventsPage = new EventsPage();
    private final EventDetailPage eventDetailPage = new EventDetailPage();

    public AdminSteps(TestContext context) {
        this.context = context;
    }

    @When("I open the Admin Events page")
    public void iOpenTheAdminEventsPage() {
        adminEventsPage.openAdminEventsPage();
    }

    @When("I submit the admin event form without required fields")
    public void iSubmitTheAdminEventFormWithoutRequiredFields() {
        adminEventsPage.submitEmptyForm();
    }

    @When("I submit an admin event with invalid price and seats")
    public void iSubmitAnAdminEventWithInvalidPriceAndSeats() {
        adminEventsPage.submitInvalidNumericValues();
    }

    @When("I create a disposable admin event through the UI")
    public void iCreateADisposableAdminEventThroughTheUi() {
        EventRequest event = TestDataFactory.event("Selenium UI Event", 6);
        context.put("adminEventTitle", event.title());
        adminEventsPage.createEvent(event);
    }

    @When("I create a one-seat admin event through the API")
    public void iCreateAOneSeatAdminEventThroughTheApi() {
        EventRequest event = TestDataFactory.event("Selenium One Seat Event", 1);
        Response response = context.apiClient().createEvent(event);
        ApiAssertions.assertSuccess(response);
        context.put("createdEventTitle", event.title());
        context.put("createdEventId", response.jsonPath().getString("data.id"));
    }

    @When("I create a one-seat event through the API")
    public void iCreateAOneSeatEventThroughTheApi() {
        iCreateAOneSeatAdminEventThroughTheApi();
    }

    @When("I create a sold-out one-seat admin event through the API")
    public void iCreateASoldOutOneSeatAdminEventThroughTheApi() {
        iCreateAOneSeatAdminEventThroughTheApi();
        String eventId = context.get("createdEventId", String.class);
        Response response = context.apiClient().createBooking(TestDataFactory.booking(eventId, 1));
        ApiAssertions.assertSuccess(response);
        context.put("createdBookingId", response.jsonPath().getString("data.id"));
    }

    @When("I book {int} ticket for the created admin event")
    public void iBookTicketForTheCreatedAdminEvent(int quantity) {
        String title = context.get("createdEventTitle", String.class);
        String eventId = context.get("createdEventId", String.class);
        eventsPage.openEventsPage();
        eventsPage.bookEvent(title);
        BookingRequest booking = TestDataFactory.booking(eventId, quantity);
        context.setLastBookingRequest(booking);
        context.put("selectedEventId", eventId);
        context.put("selectedEventTitle", title);
        eventDetailPage.fillBookingForm(booking);
        eventDetailPage.confirmBooking();
    }

    @When("I open booking details for the created admin event")
    public void iOpenBookingDetailsForTheCreatedAdminEvent() {
        String title = context.get("createdEventTitle", String.class);
        eventsPage.openEventsPage();
        eventsPage.bookEvent(title);
    }

    @When("I clean up the created admin event through API")
    public void iCleanUpTheCreatedAdminEventThroughApi() {
        context.apiClient().deleteEvent(context.get("createdEventId", String.class));
    }

    @Then("the Admin Events page should show the create form and events table")
    public void theAdminEventsPageShouldShowTheCreateFormAndEventsTable() {
        adminEventsPage.assertLoaded();
    }

    @Then("the admin form should show required field validation")
    public void theAdminFormShouldShowRequiredFieldValidation() {
        adminEventsPage.assertValidationVisible();
    }

    @Then("the admin numeric fields should show validation")
    public void theAdminNumericFieldsShouldShowValidation() {
        adminEventsPage.assertNumericValidationVisible();
    }

    @Then("the disposable admin event should appear in the admin table")
    public void theDisposableAdminEventShouldAppearInTheAdminTable() {
        adminEventsPage.assertEventVisible(context.get("adminEventTitle", String.class));
    }

    @Then("the disposable admin event should appear in event discovery")
    public void theDisposableAdminEventShouldAppearInEventDiscovery() {
        eventsPage.openEventsPage();
        eventsPage.search(context.get("adminEventTitle", String.class));
        eventsPage.assertEventVisible(context.get("adminEventTitle", String.class));
    }

    @Then("the created admin event should appear in the admin table")
    public void theCreatedAdminEventShouldAppearInTheAdminTable() {
        adminEventsPage.assertEventVisible(context.get("createdEventTitle", String.class));
    }

    @Then("the created admin event should appear in event discovery")
    public void theCreatedAdminEventShouldAppearInEventDiscovery() {
        eventsPage.openEventsPage();
        eventsPage.search(context.get("createdEventTitle", String.class));
        eventsPage.assertEventVisible(context.get("createdEventTitle", String.class));
    }

    @Then("the created admin event should show no remaining seats or be unavailable for booking")
    public void theCreatedAdminEventShouldShowNoRemainingSeatsOrBeUnavailableForBooking() {
        eventsPage.openEventsPage();
        eventsPage.search(context.get("createdEventTitle", String.class));
        eventsPage.assertEventUnavailableForBooking(context.get("createdEventTitle", String.class));
    }

    @Then("the created admin event should not appear through API or discovery")
    public void theCreatedAdminEventShouldNotAppearThroughApiOrDiscovery() {
        String eventTitle = context.get("createdEventTitle", String.class);
        eventsPage.openEventsPage();
        eventsPage.search(eventTitle);
        eventsPage.assertEventNotVisible(eventTitle);
    }
}
