package com.eventhub.automation.api;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

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
        JsonPath json = response.jsonPath();
        assertThat(json.getString("user.email")).isEqualToIgnoringCase(email);
    }

    public static void assertCurrentUser(Response response, String email) {
        assertThat(response.asString()).containsIgnoringCase(email);
    }

    public static void assertEventsInclude(Response response, String eventName) {
        assertThat(response.jsonPath().getList("data.title", String.class))
                .anySatisfy(title -> assertThat(title).contains(eventName));
    }

    public static void assertEventDetail(Response response, String eventName) {
        assertThat(response.jsonPath().getString("data.title")).isEqualTo(eventName);
    }

    public static void assertBookingReference(Response response) {
        String reference = response.jsonPath().getString("data.bookingRef");
        assertThat(reference).matches("[A-Z]-[A-Z0-9]{6}");
    }
}
