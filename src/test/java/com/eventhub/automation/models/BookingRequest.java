package com.eventhub.automation.models;

public class BookingRequest {
    private final String eventId;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final int quantity;

    public BookingRequest(String eventId, String customerName, String customerEmail, String customerPhone, int quantity) {
        this.eventId = eventId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.quantity = quantity;
    }

    public String eventId() {
        return eventId;
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

    public int quantity() {
        return quantity;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public int getQuantity() {
        return quantity;
    }
}
