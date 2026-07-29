package com.eventhub.automation.factories;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.models.EventRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static BookingRequest booking(String eventId, int quantity) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        String prefix = ConfigReader.getRequired("booking.customer.prefix");
        return new BookingRequest(
                eventId,
                prefix + " " + token,
                "selenium." + token + "@example.com",
                "+91 98765 43210",
                quantity
        );
    }

    public static EventRequest event(String titlePrefix, int seats) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        OffsetDateTime eventDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30);
        String browserDateTime = eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        return new EventRequest(
                titlePrefix + " " + token,
                "Disposable automation event created by Selenium Java framework.",
                "Technology",
                "Automation Arena",
                "Bengaluru",
                browserDateTime,
                eventDate.format(DateTimeFormatter.ISO_INSTANT),
                99,
                seats,
                "https://images.unsplash.com/photo-1511578314322-379afb476865"
        );
    }
}
