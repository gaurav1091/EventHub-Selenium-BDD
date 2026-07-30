package com.eventhub.automation.hooks;

import com.eventhub.automation.support.TestContext;
import io.cucumber.java.After;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiCleanupHooks {
    private static final Logger LOGGER = LogManager.getLogger(ApiCleanupHooks.class);
    private final TestContext context;

    public ApiCleanupHooks(TestContext context) {
        this.context = context;
    }

    @After(value = "@api-cleanup", order = 100)
    public void cleanApiCreatedData() {
        deleteBooking(context.get("createdBookingId", String.class));
        deleteEvent(context.get("createdEventId", String.class));
    }

    private void deleteBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            return;
        }
        try {
            context.apiClient().deleteBooking(bookingId);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to clean API-created booking {}", bookingId, exception);
        }
    }

    private void deleteEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        try {
            context.apiClient().deleteEvent(eventId);
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to clean API-created event {}", eventId, exception);
        }
    }
}
