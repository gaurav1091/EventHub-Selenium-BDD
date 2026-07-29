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
                "selenium." + token + "@" + TestData.text("booking.emailDomain"),
                TestData.text("booking.phone"),
                quantity
        );
    }

    public static BookingRequest invalidApiBooking() {
        return new BookingRequest(
                TestData.text("invalidApiBooking.eventId"),
                TestData.text("invalidApiBooking.customerName"),
                TestData.text("invalidApiBooking.customerEmail"),
                TestData.text("invalidApiBooking.customerPhone"),
                TestData.integer("invalidApiBooking.quantity")
        );
    }

    public static EventRequest event(String titlePrefix, int seats) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        OffsetDateTime eventDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30);
        String browserDateTime = eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        return new EventRequest(
                titlePrefix + " " + token,
                TestData.text("adminEvent.description"),
                TestData.text("adminEvent.category"),
                TestData.text("adminEvent.venue"),
                TestData.text("adminEvent.city"),
                browserDateTime,
                eventDate.format(DateTimeFormatter.ISO_INSTANT),
                TestData.integer("adminEvent.price"),
                seats,
                TestData.text("adminEvent.imageUrl")
        );
    }
}
