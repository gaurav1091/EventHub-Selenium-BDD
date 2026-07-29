package com.eventhub.automation.steps;

import com.eventhub.automation.pages.BookingsPage;
import com.eventhub.automation.pages.EventsPage;
import com.eventhub.automation.pages.HomePage;
import com.eventhub.automation.pages.NavigationBar;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class NavigationSteps {
    private final NavigationBar navigationBar = new NavigationBar();
    private final HomePage homePage = new HomePage();
    private final EventsPage eventsPage = new EventsPage();
    private final BookingsPage bookingsPage = new BookingsPage();

    @When("I open Home from the navigation")
    public void iOpenHomeFromTheNavigation() {
        navigationBar.openHome();
    }

    @When("I open Events from the navigation")
    public void iOpenEventsFromTheNavigation() {
        navigationBar.openEvents();
    }

    @When("I open My Bookings from the navigation")
    public void iOpenMyBookingsFromTheNavigation() {
        navigationBar.openBookings();
    }

    @Then("the Home page should be loaded")
    public void theHomePageShouldBeLoaded() {
        homePage.assertLoaded();
    }

    @Then("the Events page should be loaded")
    public void theEventsPageShouldBeLoaded() {
        eventsPage.assertLoaded();
    }

    @Then("the My Bookings page should be loaded")
    public void theMyBookingsPageShouldBeLoaded() {
        bookingsPage.assertLoaded();
    }
}
