package com.eventhub.automation.api;

import com.eventhub.automation.models.BookingResponse;
import com.eventhub.automation.models.EventResponse;
import com.eventhub.automation.models.LoginResponse;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

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

    public static void assertEventDetail(Response response, String eventName) {
        EventResponse event = response.jsonPath().getObject("data", EventResponse.class);
        assertThat(event.title()).isEqualTo(eventName);
        assertThat(event.id()).isNotBlank();
        assertThat(response.jsonPath().getString("data.title")).isEqualTo(eventName);
    }

    public static void assertBookingReference(Response response) {
        BookingResponse booking = response.jsonPath().getObject("data", BookingResponse.class);
        assertThat(booking.id()).isNotBlank();
        assertThat(booking.bookingRef()).matches("[A-Z]-[A-Z0-9]{6}");
        String reference = response.jsonPath().getString("data.bookingRef");
        assertThat(reference).matches("[A-Z]-[A-Z0-9]{6}");
    }

    public static void assertMatchesSchema(Response response, String schemaPath) {
        response.then().body(matchesJsonSchemaInClasspath(schemaPath));
    }

    public static void assertClientOrServerError(Response response) {
        assertThat(response.statusCode()).isBetween(400, 599);
    }
}
