package com.eventhub.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponse {
    private String id;
    private String title;
    private String description;
    private String category;
    private String venue;
    private String city;
    private String eventDate;
    private Integer price;
    private Integer totalSeats;
    private Integer availableSeats;
    private Boolean featured;

    public String id() {
        return id;
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

    public String eventDate() {
        return eventDate;
    }

    public Integer price() {
        return price;
    }

    public Integer totalSeats() {
        return totalSeats;
    }

    public Integer availableSeats() {
        return availableSeats;
    }

    public Boolean featured() {
        return featured;
    }
}
