package com.eventhub.automation.api;

import com.eventhub.automation.models.BookingResponse;
import com.eventhub.automation.models.EventResponse;
import com.eventhub.automation.models.LoginResponse;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.Arrays;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

public final class ApiAssertions {
    private ApiAssertions() {
    }

    public static void assertStatus(Response response, int expectedStatus) {
        assertThat(response.statusCode()).isEqualTo(expectedStatus);
    }

    public static void assertSuccess(Response response) {
        assertThat(response.statusCode()).isBetween(200, 299);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }

    public static void assertRegisteredIdentity(Response response, String email) {
        LoginResponse login = response.as(LoginResponse.class);
        assertThat(login.success()).isTrue();
        assertThat(login.token()).isNotBlank();
        assertThat(login.user().email()).isEqualToIgnoringCase(email);
        JsonPath json = response.jsonPath();
        assertThat(json.getString("user.email")).isEqualToIgnoringCase(email);
    }

    public static void assertCurrentUser(Response response, String email) {
        assertThat(response.asString()).containsIgnoringCase(email);
    }

    public static void assertEventsInclude(Response response, String eventName) {
        EventResponse[] events = response.jsonPath().getObject("data", EventResponse[].class);
        assertThat(events).anySatisfy(event -> assertThat(event.title()).contains(eventName));
        assertThat(response.jsonPath().getList("data.title", String.class))
                .anySatisfy(title -> assertThat(title).contains(eventName));
    }

    public static void assertEventsMapToPojoList(Response response) {
        assertSuccess(response);
        EventResponse[] events = response.jsonPath().getObject("data", EventResponse[].class);
        assertThat(events)
                .as("Expected events API response to map to EventResponse[]")
                .isNotEmpty();
        assertThat(Arrays.asList(events))
                .allSatisfy(ApiAssertions::assertEventBusinessFields);
        JsonPath json = response.jsonPath();
        assertThat(json.getList("data.id", String.class)).doesNotContainNull();
        assertThat(json.getList("data.title", String.class)).doesNotContainNull();
        assertThat(json.getList("data.city", String.class)).doesNotContainNull();
    }

    public static void assertEventDetail(Response response, String eventName) {
        EventResponse event = response.jsonPath().getObject("data", EventResponse.class);
        assertThat(event.title()).isEqualTo(eventName);
        assertThat(event.id()).isNotBlank();
        assertThat(response.jsonPath().getString("data.title")).isEqualTo(eventName);
    }

    public static void assertEventDetailMapsToPojo(Response response, String eventName) {
        assertSuccess(response);
        EventResponse event = response.jsonPath().getObject("data", EventResponse.class);
        assertEventBusinessFields(event);
        assertThat(event.title()).isEqualTo(eventName);
        JsonPath json = response.jsonPath();
        assertThat(json.getString("data.title")).isEqualTo(eventName);
        assertThat(json.getString("data.id")).isEqualTo(event.id());
        assertThat(json.getInt("data.totalSeats")).isEqualTo(event.totalSeats());
        assertThat(json.getInt("data.availableSeats")).isEqualTo(event.availableSeats());
    }

    public static void assertBookingListBusinessFields(Response response) {
        assertSuccess(response);
        BookingResponse[] bookings = response.jsonPath().getObject("data", BookingResponse[].class);
        assertThat(bookings)
                .as("Expected bookings API response to map to BookingResponse[]")
                .isNotNull();
        assertThat(Arrays.asList(bookings))
                .allSatisfy(booking -> {
                    assertThat(booking.id()).as("booking id").isNotBlank();
                    assertThat(booking.bookingRef()).as("booking reference").matches("[A-Z]-[A-Z0-9]{6}");
                    assertThat(booking.eventId()).as("event id").isNotBlank();
                    assertThat(booking.customerEmail()).as("customer email").contains("@");
                    assertThat(booking.quantity()).as("booking quantity").isPositive();
                    if (booking.totalAmount() != null) {
                        assertThat(booking.totalAmount()).as("booking total amount").isGreaterThanOrEqualTo(0);
                    }
                });
    }

    public static void assertBookingReference(Response response) {
        BookingResponse booking = response.jsonPath().getObject("data", BookingResponse.class);
        assertThat(booking.id()).isNotBlank();
        assertThat(booking.bookingRef()).matches("[A-Z]-[A-Z0-9]{6}");
        String reference = response.jsonPath().getString("data.bookingRef");
        assertThat(reference).matches("[A-Z]-[A-Z0-9]{6}");
    }

    public static void assertBookingsInclude(Response response, String bookingId, String customerEmail) {
        assertSuccess(response);
        BookingResponse[] bookings = response.jsonPath().getObject("data", BookingResponse[].class);
        assertThat(bookings)
                .as("Expected bookings API response to map to BookingResponse[]")
                .isNotEmpty();
        assertThat(Arrays.asList(bookings))
                .anySatisfy(booking -> {
                    assertThat(booking.id()).isEqualTo(bookingId);
                    assertThat(booking.customerEmail()).isEqualToIgnoringCase(customerEmail);
                    assertThat(booking.bookingRef()).matches("[A-Z]-[A-Z0-9]{6}");
                });
        assertThat(response.jsonPath().getList("data.customerEmail", String.class))
                .anySatisfy(email -> assertThat(email).isEqualToIgnoringCase(customerEmail));
    }

    public static void assertBookingsIncludeCustomer(Response response, String customerEmail, String customerName) {
        assertSuccess(response);
        BookingResponse[] bookings = response.jsonPath().getObject("data", BookingResponse[].class);
        assertThat(Arrays.asList(bookings))
                .as("Expected API booking list to include customer %s", customerEmail)
                .anySatisfy(booking -> {
                    assertThat(booking.customerEmail()).isEqualToIgnoringCase(customerEmail);
                    assertThat(booking.customerName()).isEqualTo(customerName);
                    assertThat(booking.bookingRef()).matches("[A-Z]-[A-Z0-9]{6}");
                });
    }

    public static void assertBookingCreateOrBusinessRejection(Response response) {
        assertThat(response.statusCode()).isBetween(200, 409);
        if (response.statusCode() < 400) {
            assertBookingReference(response);
        } else {
            assertMatchesSchema(response, "schemas/error-response.schema.json");
        }
    }

    public static void assertMatchesSchema(Response response, String schemaPath) {
        response.then().body(matchesJsonSchemaInClasspath(schemaPath));
    }

    public static void assertClientOrServerError(Response response) {
        assertThat(response.statusCode()).isBetween(400, 599);
    }

    private static void assertEventBusinessFields(EventResponse event) {
        assertThat(event.id()).isNotBlank();
        assertThat(event.title()).isNotBlank();
        assertThat(event.description()).isNotBlank();
        assertThat(event.category()).isNotBlank();
        assertThat(event.venue()).isNotBlank();
        assertThat(event.city()).isNotBlank();
        assertThat(event.eventDate()).isNotBlank();
        assertThat(event.price()).isNotNegative();
        assertThat(event.totalSeats()).isPositive();
        assertThat(event.availableSeats()).isBetween(0, event.totalSeats());
    }
}
