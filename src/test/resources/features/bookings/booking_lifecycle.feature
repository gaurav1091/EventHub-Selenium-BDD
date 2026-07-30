@bookings @ui
Feature: Event booking lifecycle

  Background:
    Given I am signed in to EventHub

  @smoke @stateful
  Scenario: User can book a ticket and view it under My Bookings
    When I book 1 ticket for a bookable event
    Then I should see the booking confirmation
    When I open My Bookings from the confirmation
    Then I should see booking for the selected event

  @regression @stateful
  Scenario Outline: User can book tickets and see confirmation
    When I book <quantity> tickets for a bookable event
    Then I should see the booking confirmation

    Examples:
      | quantity |
      | 1        |
      | 2        |

  @regression @stateful
  Scenario: User can view booking details
    When I book 1 ticket for a bookable event
    And I open My Bookings from the confirmation
    And I open the booking details
    Then I should see booking for the selected event

  @regression @stateful
  Scenario: User can cancel a booking
    When I book 1 ticket for a bookable event
    And I open My Bookings from the confirmation
    And I cancel the booking
    Then no bookings for the current Selenium customer should remain

  @regression @stateful
  Scenario: User can clear all bookings
    When I book 1 ticket for a bookable event
    And I open My Bookings from the confirmation
    And I clear all bookings
    Then no bookings for the current Selenium customer should remain

  @regression @parallel-safe
  Scenario: Event detail page shows booking form and event content
    When I open details for a bookable event
    Then I should see the event detail booking panel

  @regression @parallel-safe
  Scenario: Ticket quantity controls update the booking total
    When I open details for an event with at least 4 available tickets
    Then the ticket quantity should be 1
    And the ticket decrement control should be disabled
    When I increase tickets by 1
    Then the ticket quantity should be 2
    When I decrease tickets by 1
    Then the ticket quantity should be 1

  @regression @stateful @api-cleanup
  Scenario: Booking cannot exceed available tickets
    When I create a one-seat admin event through the API
    And I open booking details for the created admin event
    Then the ticket quantity should be 1
    And the ticket decrement control should be disabled
    And the ticket increment control should be disabled

  @regression @parallel-safe
  Scenario Outline: Booking form validates invalid customer data
    When I open details for a bookable event
    And I enter booking customer email "<email>" and phone "<phone>"
    Then the <field> field should be invalid

    Examples:
      | email                     | phone           | field         |
      | invalid-email             | +91 98765 43210 | booking email |
      | gauravarora1091@gmail.com | 123             | booking phone |

  @regression @parallel-safe
  Scenario: Booking form validates required customer details
    When I open details for a bookable event
    And I submit the booking form without customer details
    Then the booking form should show required field validation

  @regression @parallel-safe
  Scenario: Booking form accepts generated valid customer data before submit
    When I enter valid booking customer details for a bookable event
    Then the booking form should contain the generated customer details

  @regression @stateful
  Scenario: Selenium-created bookings can be cleaned through API
    When I create a booking through the API for cleanup
    And I clean Selenium-created bookings through the API
    Then no bookings for the current Selenium customer should remain
