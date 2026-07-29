package com.eventhub.automation.models;

public class EventRequest {
    private final String title;
    private final String description;
    private final String category;
    private final String venue;
    private final String city;
    private final String dateTime;
    private final String eventDate;
    private final int price;
    private final int totalSeats;
    private final String imageUrl;

    public EventRequest(String title, String description, String category, String venue, String city,
                        String dateTime, String eventDate, int price, int totalSeats, String imageUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.city = city;
        this.dateTime = dateTime;
        this.eventDate = eventDate;
        this.price = price;
        this.totalSeats = totalSeats;
        this.imageUrl = imageUrl;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String category() {
        return category;
    }

    public String venue() {
        return venue;
    }

    public String city() {
        return city;
    }

    public String dateTime() {
        return dateTime;
    }

    public String eventDate() {
        return eventDate;
    }

    public int price() {
        return price;
    }

    public int totalSeats() {
        return totalSeats;
    }

    public String imageUrl() {
        return imageUrl;
    }
}
