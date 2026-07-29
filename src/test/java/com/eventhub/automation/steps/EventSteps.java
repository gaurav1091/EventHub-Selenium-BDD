package com.eventhub.automation.steps;

import com.eventhub.automation.pages.EventDetailPage;
import com.eventhub.automation.pages.EventsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class EventSteps {
    private final EventsPage eventsPage = new EventsPage();
    private final EventDetailPage eventDetailPage = new EventDetailPage();

    @When("I open the Events page")
    public void iOpenTheEventsPage() {
        eventsPage.openEventsPage();
    }

    @When("I search events for {string}")
    public void iSearchEventsFor(String query) {
        eventsPage.search(query);
    }

    @When("I filter events by category {string}")
    public void iFilterEventsByCategory(String category) {
        eventsPage.filterByCategory(category);
    }

    @When("I filter events by city {string}")
    public void iFilterEventsByCity(String city) {
        eventsPage.filterByCity(city);
    }

    @When("I clear event filters")
    public void iClearEventFilters() {
        eventsPage.clearFilters();
    }

    @When("I open details from the title for event {string}")
    public void iOpenDetailsFromTheTitleForEvent(String eventName) {
        eventsPage.openEventsPage();
        eventsPage.openEventByTitle(eventName);
    }

    @When("I open details from Book Now for event {string}")
    public void iOpenDetailsFromBookNowForEvent(String eventName) {
        eventsPage.openEventsPage();
        eventsPage.bookEvent(eventName);
    }

    @When("I open details from Book Now for a bookable event")
    public void iOpenDetailsFromBookNowForABookableEvent() {
        eventsPage.openEventsPage();
        eventsPage.bookFirstAvailableEvent();
    }

    @Then("I should see seeded upcoming events")
    public void iShouldSeeSeededUpcomingEvents() {
        eventsPage.assertSeededEventsVisible();
    }

    @Then("I should see event {string}")
    public void iShouldSeeEvent(String eventName) {
        eventsPage.assertEventVisible(eventName);
    }

    @Then("I should not see event {string}")
    public void iShouldNotSeeEvent(String eventName) {
        eventsPage.assertEventNotVisible(eventName);
    }

    @Then("I should see the no events found message")
    public void iShouldSeeTheNoEventsFoundMessage() {
        eventsPage.assertNoEventsFound();
    }

    @Then("event {string} card should show category {string}, city {string}, price {string}, seats, and availability status")
    public void eventCardShouldShowBusinessDetails(String eventName, String category, String city, String price) {
        eventsPage.assertEventCardDetails(eventName, category, city, price);
        eventsPage.assertEventCardAvailability(eventName);
    }

    @Then("I should see metadata for event {string}")
    public void iShouldSeeMetadataForEvent(String eventName) {
        eventDetailPage.assertMetadataFor(eventName);
    }
}
