package com.eventhub.automation.support;

import com.eventhub.automation.api.EventHubApiClient;
import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.models.BookingRequest;
import com.eventhub.automation.models.EventResponse;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private final EventHubApiClient apiClient = new EventHubApiClient(ConfigReader.getRequired("api.base.url"));
    private final Map<String, Object> values = new HashMap<>();
    private BookingRequest lastBookingRequest;
    private EventResponse selectedEvent;

    public EventHubApiClient apiClient() {
        return apiClient;
    }

    public void put(String key, Object value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) values.get(key);
    }

    public BookingRequest lastBookingRequest() {
        return lastBookingRequest;
    }

    public void setLastBookingRequest(BookingRequest lastBookingRequest) {
        this.lastBookingRequest = lastBookingRequest;
    }

    public EventResponse selectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(EventResponse selectedEvent) {
        this.selectedEvent = selectedEvent;
    }
}
