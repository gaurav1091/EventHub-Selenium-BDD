@hybrid @ui @stateful
Feature: API and UI hybrid coverage

  Background:
    Given I am signed in to EventHub

  @regression
  Scenario: Booking created through API is visible in My Bookings
    When I create a booking through the API for cleanup
    And I directly open protected route "/bookings"
    Then I should see booking for the selected event
    When I clean Selenium-created bookings through the API
    Then no bookings for the current Selenium customer should remain
