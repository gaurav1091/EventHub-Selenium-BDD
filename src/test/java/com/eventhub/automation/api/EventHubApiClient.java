package com.eventhub.automation.api;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.models.EventRequest;
import com.eventhub.automation.models.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EventHubApiClient {
    private final String apiBaseUrl;
    private String token;

    public EventHubApiClient(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public Response login() {
        return login(ConfigReader.getRequired("user.email"), ConfigReader.getRequired("user.password"));
    }

    public Response login(String email, String password) {
        Response response = baseRequest()
                .body(new LoginRequest(email, password))
                .post("/api/auth/login");
        if (response.statusCode() == 200) {
            token = response.jsonPath().getString("token");
        }
        return response;
    }

    public Response currentUser() {
        return authenticatedRequest().get("/api/auth/me");
    }

    public Response events() {
        return authenticatedRequest().get("/api/events");
    }

    public Response event(String eventId) {
        return authenticatedRequest().get("/api/events/{eventId}", eventId);
    }

    public Response createEvent(EventRequest event) {
        Map<String, Object> payload = Map.of(
                "title", event.title(),
                "description", event.description(),
                "category", event.category(),
                "venue", event.venue(),
                "city", event.city(),
                "eventDate", event.eventDate(),
                "price", event.price(),
                "totalSeats", event.totalSeats(),
                "imageUrl", event.imageUrl()
        );
        return authenticatedRequest().body(payload).post("/api/events");
    }

    public Response createEvent(Map<String, ?> payload) {
        return authenticatedRequest().body(payload).post("/api/events");
    }

    public Response deleteEvent(String eventId) {
        return authenticatedRequest().delete("/api/events/{eventId}", eventId);
    }

    public Response bookings() {
        return authenticatedRequest().get("/api/bookings");
    }

    public Response bookings(Map<String, ?> queryParams) {
        return authenticatedRequest().queryParams(queryParams).get("/api/bookings");
    }

    public Response createBooking(BookingRequest booking) {
        return authenticatedRequest().body(booking).post("/api/bookings");
    }

    public Response createBooking(Map<String, ?> payload) {
        return authenticatedRequest().body(payload).post("/api/bookings");
    }

    public Response deleteBooking(String bookingId) {
        return authenticatedRequest().delete("/api/bookings/{bookingId}", bookingId);
    }

    public Response health() {
        return baseRequest().get("/api/health");
    }

    public Response anonymousBookings() {
        return baseRequest().get("/api/bookings");
    }

    public Response anonymousCreateBooking(BookingRequest booking) {
        return baseRequest().body(booking).post("/api/bookings");
    }

    public Response anonymousCurrentUser() {
        return baseRequest().get("/api/auth/me");
    }

    public void ensureAuthenticated() {
        if (token == null || token.isBlank()) {
            login();
        }
    }

    public String findEventIdByTitle(String title) {
        Response response = events();
        return response.jsonPath().getString("data.find { it.title.contains('" + title.replace("'", "\\'") + "') }.id");
    }

    public String findEventTitleById(String eventId) {
        Response response = events();
        List<Map<String, Object>> events = response.jsonPath().getList("data");
        return events.stream()
                .filter(event -> Objects.equals(String.valueOf(event.get("id")), eventId))
                .map(event -> String.valueOf(event.get("title")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No event found for id: " + eventId));
    }

    public int findEventPriceById(String eventId) {
        Response response = events();
        List<Map<String, Object>> events = response.jsonPath().getList("data");
        return events.stream()
                .filter(event -> Objects.equals(String.valueOf(event.get("id")), eventId))
                .map(event -> event.get("price"))
                .map(EventHubApiClient::integerValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> {
                    Object price = event(eventId).jsonPath().get("data.price");
                    Integer parsedPrice = integerValue(price);
                    if (parsedPrice == null) {
                        throw new IllegalArgumentException("No event price found for id: " + eventId);
                    }
                    return parsedPrice;
                });
    }

    public String findFirstBookableEventId() {
        Response response = events();
        List<Map<String, Object>> events = response.jsonPath().getList("data");
        return events.stream()
                .filter(event -> seats(event) > 0)
                .map(event -> String.valueOf(event.get("id")))
                .findFirst()
                .orElseGet(() -> findEventIdByTitle("World Tech Summit"));
    }

    public String findBookableEventTitleWithSeatsAtLeast(int minimumSeats) {
        Response response = events();
        List<Map<String, Object>> events = response.jsonPath().getList("data");
        return events.stream()
                .filter(event -> seats(event) >= minimumSeats)
                .map(event -> String.valueOf(event.get("title")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No bookable event found with at least "
                        + minimumSeats + " available seats"));
    }

    public JsonPath json(Response response) {
        return response.jsonPath();
    }

    private RequestSpecification authenticatedRequest() {
        ensureAuthenticated();
        return baseRequest().header("Authorization", "Bearer " + token);
    }

    private RequestSpecification baseRequest() {
        return RestAssured.given()
                .baseUri(apiBaseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .relaxedHTTPSValidation();
    }

    private int seats(Map<String, Object> event) {
        Object availableSeats = event.get("availableSeats");
        if (availableSeats == null) {
            availableSeats = event.get("seatsLeft");
        }
        if (availableSeats instanceof Number) {
            return ((Number) availableSeats).intValue();
        }
        return 0;
    }

    private static Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            String digits = text.replaceAll("[^0-9]", "");
            return digits.isBlank() ? null : Integer.parseInt(digits);
        }
        return null;
    }
}
