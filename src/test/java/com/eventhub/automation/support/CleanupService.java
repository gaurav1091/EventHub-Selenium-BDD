package com.eventhub.automation.support;

import com.eventhub.automation.api.EventHubApiClient;
import com.eventhub.automation.config.ConfigReader;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class CleanupService {
    private static final Logger LOGGER = LogManager.getLogger(CleanupService.class);
    private final EventHubApiClient apiClient;

    public CleanupService(EventHubApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void deleteBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            return;
        }
        try {
            apiClient.deleteBooking(bookingId);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to clean booking {}", bookingId, exception);
        }
    }

    public void deleteEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        try {
            apiClient.deleteEvent(eventId);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to clean event {}", eventId, exception);
        }
    }

    public void deleteBookingsByCustomerPrefix(String customerPrefix) {
        Response response = apiClient.bookings();
        List<String> bookingIds = response.jsonPath()
                .getList("data.findAll { it.customerName != null && it.customerName.startsWith('"
                        + customerPrefix.replace("'", "\\'") + "') }.id", String.class);
        bookingIds.forEach(this::deleteBooking);
    }

    public void deleteEventsByTitlePrefix(String eventTitlePrefix) {
        Response response = apiClient.events();
        List<Map<String, Object>> events = response.jsonPath().getList("data");
        events.stream()
                .filter(event -> String.valueOf(event.get("title")).startsWith(eventTitlePrefix))
                .map(event -> String.valueOf(event.get("id")))
                .forEach(this::deleteEvent);
    }

    public void cleanCurrentRunData() {
        deleteBookingsByCustomerPrefix(RunContext.bookingCustomerPrefix());
        deleteEventsByTitlePrefix(RunContext.eventTitlePrefix(ConfigReader.getRequired("event.cleanup.prefix")));
        deleteEventsByTitlePrefix(RunContext.eventTitlePrefix("Selenium One Seat Event"));
        deleteEventsByTitlePrefix(RunContext.eventTitlePrefix("Selenium UI Event"));
    }
}
