@admin @ui
Feature: Admin event management

  Background:
    Given I am signed in to EventHub

  @smoke @parallel-safe
  Scenario: Admin Events page renders management controls
    When I open the Admin Events page
    Then the Admin Events page should show the create form and events table

  @regression @parallel-safe
  Scenario: Admin event form validates required fields
    When I open the Admin Events page
    And I submit the admin event form without required fields
    Then the admin form should show required field validation

  @regression @stateful
  Scenario: Admin can create a disposable event and find it in discovery
    When I create a one-seat admin event through the API
    And I open the Admin Events page
    Then the created admin event should appear in the admin table
    And the created admin event should appear in event discovery
