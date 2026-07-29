package com.eventhub.automation.steps;

import com.eventhub.automation.api.ApiAssertions;
import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.factories.TestDataFactory;
import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.support.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSteps {
    private final TestContext context;

    public ApiSteps(TestContext context) {
        this.context = context;
    }

    @When("I request the API health endpoint")
    public void iRequestTheApiHealthEndpoint() {
        context.put("response", context.apiClient().health());
    }

    @When("I authenticate through the API")
    public void iAuthenticateThroughTheApi() {
        context.put("response", context.apiClient().login());
    }

    @When("I authenticate through the API with invalid credentials")
    public void iAuthenticateThroughTheApiWithInvalidCredentials() {
        context.put("response", context.apiClient().login(ConfigReader.getRequired("user.email"), "WrongPassword@123"));
    }

    @When("I request the current user profile through the API")
    public void iRequestTheCurrentUserProfileThroughTheApi() {
        context.put("response", context.apiClient().currentUser());
    }

    @When("I request events through the API")
    public void iRequestEventsThroughTheApi() {
        context.put("response", context.apiClient().events());
    }

    @When("I request event {string} through the API")
    public void iRequestEventThroughTheApi(String eventName) {
        String eventId = context.apiClient().findEventIdByTitle(eventName);
        context.put("response", context.apiClient().event(eventId));
    }

    @When("I create a booking through the API")
    public void iCreateABookingThroughTheApi() {
        String eventId = context.apiClient().findFirstBookableEventId();
        BookingRequest booking = TestDataFactory.booking(eventId, 1);
        context.setLastBookingRequest(booking);
        context.put("response", context.apiClient().createBooking(booking));
    }

    @When("I cancel the API-created booking")
    public void iCancelTheApiCreatedBooking() {
        Response previous = context.get("response", Response.class);
        String bookingId = previous.jsonPath().getString("data.id");
        context.put("response", context.apiClient().deleteBooking(bookingId));
    }

    @When("I request bookings through the API without authentication")
    public void iRequestBookingsThroughTheApiWithoutAuthentication() {
        context.put("response", context.apiClient().anonymousBookings());
    }

    @When("I create a booking through the API without authentication")
    public void iCreateABookingThroughTheApiWithoutAuthentication() {
        BookingRequest booking = TestDataFactory.booking("unknown", 1);
        context.put("response", context.apiClient().anonymousCreateBooking(booking));
    }

    @When("I request unknown event detail through the API")
    public void iRequestUnknownEventDetailThroughTheApi() {
        context.put("response", context.apiClient().event("unknown-event-id"));
    }

    @When("I cancel an unknown booking through the API")
    public void iCancelAnUnknownBookingThroughTheApi() {
        context.put("response", context.apiClient().deleteBooking("unknown-booking-id"));
    }

    @When("I create a booking through the API with invalid payload")
    public void iCreateABookingThroughTheApiWithInvalidPayload() {
        BookingRequest booking = new BookingRequest("", "", "bad-email", "123", 0);
        context.put("response", context.apiClient().createBooking(booking));
    }

    @Then("the API health response should be successful")
    public void theApiHealthResponseShouldBeSuccessful() {
        ApiAssertions.assertStatus(context.get("response", Response.class), 200);
    }

    @Then("the internal API contract should document the automated endpoints")
    public void theInternalApiContractShouldDocumentTheAutomatedEndpoints() {
        assertThat(getClass().getClassLoader().getResource("contracts/eventhub-api-contract.json")).isNotNull();
    }

    @Then("the API should return the registered user identity")
    public void theApiShouldReturnTheRegisteredUserIdentity() {
        Response response = context.get("response", Response.class);
        ApiAssertions.assertSuccess(response);
        ApiAssertions.assertRegisteredIdentity(response, ConfigReader.getRequired("user.email"));
    }

    @Then("the API current user response should include the registered identity")
    public void theApiCurrentUserResponseShouldIncludeTheRegisteredIdentity() {
        Response response = context.get("response", Response.class);
        ApiAssertions.assertSuccess(response);
        ApiAssertions.assertCurrentUser(response, ConfigReader.getRequired("user.email"));
    }

    @Then("the API events response should include seeded EventHub events")
    public void theApiEventsResponseShouldIncludeSeededEventHubEvents() {
        Response response = context.get("response", Response.class);
        ApiAssertions.assertSuccess(response);
        ApiAssertions.assertEventsInclude(response, "Dilli Diwali Mela");
        ApiAssertions.assertEventsInclude(response, "Hollywood Monsoon Night");
        ApiAssertions.assertEventsInclude(response, "World Tech Summit");
    }

    @Then("the API event detail response should describe {string}")
    public void theApiEventDetailResponseShouldDescribe(String eventName) {
        ApiAssertions.assertSuccess(context.get("response", Response.class));
        ApiAssertions.assertEventDetail(context.get("response", Response.class), eventName);
    }

    @Then("the API booking response should include a booking reference")
    public void theApiBookingResponseShouldIncludeABookingReference() {
        Response response = context.get("response", Response.class);
        ApiAssertions.assertSuccess(response);
        ApiAssertions.assertBookingReference(response);
    }

    @Then("the API booking cancellation should be successful")
    public void theApiBookingCancellationShouldBeSuccessful() {
        ApiAssertions.assertStatus(context.get("response", Response.class), 200);
    }

    @Then("the API should reject the request with status {int}")
    public void theApiShouldRejectTheRequestWithStatus(int status) {
        ApiAssertions.assertStatus(context.get("response", Response.class), status);
    }
}
