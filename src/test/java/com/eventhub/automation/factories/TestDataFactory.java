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
        return bookingBuilder(eventId)
                .customerName(ConfigReader.getRequired("booking.customer.prefix") + " " + token)
                .customerEmail("selenium." + token + "@" + TestData.text("booking.emailDomain"))
                .quantity(quantity)
                .build();
    }

    public static BookingRequest invalidApiBooking() {
        return bookingBuilder(TestData.text("invalidApiBooking.eventId"))
                .customerName(TestData.text("invalidApiBooking.customerName"))
                .customerEmail(TestData.text("invalidApiBooking.customerEmail"))
                .customerPhone(TestData.text("invalidApiBooking.customerPhone"))
                .quantity(TestData.integer("invalidApiBooking.quantity"))
                .build();
    }

    public static EventRequest event(String titlePrefix, int seats) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        OffsetDateTime eventDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30);
        return eventBuilder(titlePrefix + " " + token)
                .eventDate(eventDate)
                .totalSeats(seats)
                .build();
    }

    public static BookingRequestBuilder bookingBuilder(String eventId) {
        return new BookingRequestBuilder(eventId);
    }

    public static EventRequestBuilder eventBuilder(String title) {
        return new EventRequestBuilder(title);
    }

    public static final class BookingRequestBuilder {
        private final String eventId;
        private String customerName = ConfigReader.getRequired("booking.customer.prefix");
        private String customerEmail = "selenium.booking@" + TestData.text("booking.emailDomain");
        private String customerPhone = TestData.text("booking.phone");
        private int quantity = 1;

        private BookingRequestBuilder(String eventId) {
            this.eventId = eventId;
        }

        public BookingRequestBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public BookingRequestBuilder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public BookingRequestBuilder customerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
            return this;
        }

        public BookingRequestBuilder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public BookingRequest build() {
            return new BookingRequest(eventId, customerName, customerEmail, customerPhone, quantity);
        }
    }

    public static final class EventRequestBuilder {
        private final String title;
        private String description = TestData.text("adminEvent.description");
        private String category = TestData.text("adminEvent.category");
        private String venue = TestData.text("adminEvent.venue");
        private String city = TestData.text("adminEvent.city");
        private OffsetDateTime eventDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30);
        private int price = TestData.integer("adminEvent.price");
        private int totalSeats = 5;
        private String imageUrl = TestData.text("adminEvent.imageUrl");

        private EventRequestBuilder(String title) {
            this.title = title;
        }

        public EventRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventRequestBuilder category(String category) {
            this.category = category;
            return this;
        }

        public EventRequestBuilder venue(String venue) {
            this.venue = venue;
            return this;
        }

        public EventRequestBuilder city(String city) {
            this.city = city;
            return this;
        }

        public EventRequestBuilder eventDate(OffsetDateTime eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public EventRequestBuilder price(int price) {
            this.price = price;
            return this;
        }

        public EventRequestBuilder totalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public EventRequestBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public EventRequest build() {
            return new EventRequest(
                    title,
                    description,
                    category,
                    venue,
                    city,
                    eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                    eventDate.format(DateTimeFormatter.ISO_INSTANT),
                    price,
                    totalSeats,
                    imageUrl
            );
        }
    }
}
