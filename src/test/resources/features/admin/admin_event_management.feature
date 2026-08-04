@admin @ui @owner-platform @risk-admin @intent-admin @impact-admin
Feature: Admin event management

  Background:
    Given I am signed in to EventHub

  @p0 @smoke @parallel-safe @critical
  Scenario: Admin Events page renders management controls
    When I open the Admin Events page
    Then the Admin Events page should show the create form and events table

  @p1 @regression @parallel-safe @intent-validation
  Scenario: Admin event form validates required fields
    When I open the Admin Events page
    And I submit the admin event form without required fields
    Then the admin form should show required field validation

  @p1 @regression @parallel-safe @negative @intent-validation
  Scenario: Admin event form rejects invalid numeric values
    When I open the Admin Events page
    And I submit an admin event with invalid price and seats
    Then the admin numeric fields should show validation

  @p1 @regression @stateful @api-cleanup
  Scenario: Admin can create a disposable event and find it in discovery
    When I create a one-seat admin event through the API
    And I open the Admin Events page
    Then the created admin event should appear in the admin table
    And the created admin event should appear in event discovery
    When I clean up the created admin event through API
    Then the created admin event should not appear through API or discovery

  @p1 @regression @stateful @api-cleanup @negative @intent-capacity
  Scenario: Sold-out admin event is unavailable from discovery
    When I create a sold-out one-seat admin event through the API
    Then the created admin event should show no remaining seats or be unavailable for booking
