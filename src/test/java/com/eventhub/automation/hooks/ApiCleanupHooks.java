package com.eventhub.automation.hooks;

import com.eventhub.automation.support.CleanupService;
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
        CleanupService cleanup = new CleanupService(context.apiClient());
        try {
            cleanup.deleteBooking(context.get("createdBookingId", String.class));
            cleanup.deleteEvent(context.get("createdEventId", String.class));
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to clean API-created scenario data", exception);
        }
    }
}
