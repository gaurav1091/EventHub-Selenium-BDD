package com.eventhub.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {
    private String id;
    private String bookingRef;
    private String eventId;
    private String eventTitle;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer quantity;
    private Integer totalAmount;
    private String status;

    public String id() {
        return id;
    }

    public String bookingRef() {
        return bookingRef;
    }

    public String eventId() {
        return eventId;
    }

    public String eventTitle() {
        return eventTitle;
    }

    public String customerName() {
        return customerName;
    }

    public String customerEmail() {
        return customerEmail;
    }

    public String customerPhone() {
        return customerPhone;
    }

    public Integer quantity() {
        return quantity;
    }

    public Integer totalAmount() {
        return totalAmount;
    }

    public String status() {
        return status;
    }
}
