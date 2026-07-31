@hybrid @ui @stateful @owner-platform @risk-integration @intent-hybrid @impact-integration
Feature: API and UI hybrid coverage

  Background:
    Given I am signed in to EventHub

  @p1 @regression
  Scenario: Booking created through API is visible in My Bookings
    When I create a booking through the API for cleanup
    And I directly open protected route "/bookings"
    Then I should see booking for the selected event
    When I clean Selenium-created bookings through the API
    Then no bookings for the current Selenium customer should remain

  @p1 @regression @api-cleanup
  Scenario: API-created event is searchable and deep-linkable in the UI
    When I create a one-seat admin event through the API
    Then the created admin event should appear in event discovery
    When I directly open event detail for the created admin event
    Then I should see metadata for the created admin event

  @p1 @regression @api-cleanup
  Scenario: API-created event appears in admin management and discovery
    When I create a one-seat admin event through the API
    And I open the Admin Events page
    Then the created admin event should appear in the admin table
    And the created admin event should appear in event discovery

  @p1 @regression @api-cleanup
  Scenario: UI booking for API-created event is visible through the API
    When I create a one-seat admin event through the API
    And I book 1 ticket for the created admin event
    Then I should see the booking confirmation
    When I request bookings through the API
    Then the API bookings response should include the UI-created booking

  @p1 @regression @api-cleanup
  Scenario: API cleanup of an API-created booking is reflected in the UI
    When I create a booking through the API for cleanup
    And I directly open protected route "/bookings"
    Then I should see booking for the selected event
    When I clean Selenium-created bookings through the API
    And I directly open protected route "/bookings"
    Then no bookings for the current Selenium customer should remain

  @p1 @regression
  Scenario: API-created booking details can be opened in the UI
    When I create a booking through the API for cleanup
    And I directly open protected route "/bookings"
    And I open the booking details
    Then I should see booking for the selected event
    When I clean Selenium-created bookings through the API

  @p1 @regression @negative @api-cleanup
  Scenario: API-created sold-out event is unavailable in event discovery
    When I create a sold-out one-seat admin event through the API
    Then the created admin event should show no remaining seats or be unavailable for booking
