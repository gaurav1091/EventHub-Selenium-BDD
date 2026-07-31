@bookings @ui @owner-platform @risk-revenue @intent-booking @impact-bookings
Feature: Event booking lifecycle

  Background:
    Given I am signed in to EventHub

  @p0 @smoke @stateful @critical
  Scenario: User can book a ticket and view it under My Bookings
    When I book 1 ticket for a bookable event
    Then I should see the booking confirmation
    When I open My Bookings from the confirmation
    Then I should see booking for the selected event

  @p1 @regression @stateful
  Scenario Outline: User can book tickets and see confirmation
    When I book <quantity> tickets for a bookable event
    Then I should see the booking confirmation

    Examples:
      | quantity |
      | 1        |
      | 2        |

  @p1 @regression @stateful @critical
  Scenario: Booking confirmation shows customer, quantity, event, and total
    When I book 2 tickets for a bookable event
    Then the booking confirmation should show the selected event, customer, quantity, and total

  @p1 @regression @stateful
  Scenario: User can view booking details
    When I book 1 ticket for a bookable event
    And I open My Bookings from the confirmation
    And I open the booking details
    Then I should see booking for the selected event

  @p1 @regression @stateful @critical
  Scenario: User can cancel a booking
    When I book 1 ticket for a bookable event
    And I open My Bookings from the confirmation
    And I cancel the booking
    Then no bookings for the current Selenium customer should remain

  @p1 @regression @stateful
  Scenario: User can clear all bookings
    When I book 1 ticket for a bookable event
    And I open My Bookings from the confirmation
    And I clear all bookings
    Then no bookings for the current Selenium customer should remain

  @p1 @regression @parallel-safe
  Scenario: Event detail page shows booking form and event content
    When I open details for a bookable event
    Then I should see the event detail booking panel

  @p1 @regression @parallel-safe
  Scenario: Ticket quantity controls update the booking total
    When I open details for an event with at least 4 available tickets
    Then the ticket quantity should be 1
    And the ticket decrement control should be disabled
    When I increase tickets by 1
    Then the ticket quantity should be 2
    When I decrease tickets by 1
    Then the ticket quantity should be 1

  @p1 @regression @stateful @api-cleanup @negative
  Scenario: Booking cannot exceed available tickets
    When I create a one-seat admin event through the API
    And I open booking details for the created admin event
    Then the ticket quantity should be 1
    And the ticket decrement control should be disabled
    And the ticket increment control should be disabled

  @p1 @regression @parallel-safe @negative @intent-validation
  Scenario Outline: Booking form validates invalid customer data
    When I open details for a bookable event
    And I enter booking customer email "<email>" and phone "<phone>"
    Then the <field> field should be invalid

    Examples:
      | email                     | phone           | field         |
      | invalid-email             | +91 98765 43210 | booking email |
      | gauravarora1091@gmail.com | 123             | booking phone |

  @p1 @regression @parallel-safe @intent-validation
  Scenario: Booking form validates required customer details
    When I open details for a bookable event
    And I submit the booking form without customer details
    Then the booking form should show required field validation

  @p2 @regression @parallel-safe @intent-validation
  Scenario: Booking form accepts generated valid customer data before submit
    When I enter valid booking customer details for a bookable event
    Then the booking form should contain the generated customer details

  @p1 @regression @stateful @intent-cleanup
  Scenario: Selenium-created bookings can be cleaned through API
    When I create a booking through the API for cleanup
    And I clean Selenium-created bookings through the API
    Then no bookings for the current Selenium customer should remain
    And My Bookings should show an empty state
