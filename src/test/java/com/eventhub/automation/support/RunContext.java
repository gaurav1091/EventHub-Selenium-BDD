package com.eventhub.automation.support;

import com.eventhub.automation.config.ConfigReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class RunContext {
    private static final String RUN_ID = resolveRunId();

    private RunContext() {
    }

    public static String id() {
        return RUN_ID;
    }

    public static String bookingCustomerPrefix() {
        return ConfigReader.getRequired("booking.customer.prefix") + " " + RUN_ID;
    }

    public static String eventTitlePrefix(String basePrefix) {
        return basePrefix + " " + RUN_ID;
    }

    private static String resolveRunId() {
        String configured = ConfigReader.get("run.id");
        if (configured != null && !configured.isBlank()) {
            return safe(configured);
        }
        return "run-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private static String safe(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
    }
}
