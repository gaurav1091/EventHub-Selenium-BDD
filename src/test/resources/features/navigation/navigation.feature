@navigation @ui
Feature: EventHub navigation

  Background:
    Given I am signed in to EventHub

  @regression @parallel-safe
  Scenario: Authenticated user can move across primary navigation
    When I open Home from the navigation
    Then the Home page should be loaded
    When I open Events from the navigation
    Then the Events page should be loaded
    When I open My Bookings from the navigation
    Then the My Bookings page should be loaded
