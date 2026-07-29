@events @ui
Feature: Event discovery

  Background:
    Given I am signed in to EventHub

  @smoke @parallel-safe
  Scenario: User can view upcoming events
    When I open the Events page
    Then I should see seeded upcoming events

  @regression @parallel-safe
  Scenario Outline: User can search for events by keyword
    When I open the Events page
    And I search events for "<query>"
    Then I should see event "<expectedEvent>"

    Examples:
      | query              | expectedEvent      |
      | Dilli Diwali Mela  | Dilli Diwali Mela  |
      | Pragati Maidan     | Dilli Diwali Mela  |
      | World Tech Summit  | World Tech Summit  |

  @regression @parallel-safe
  Scenario: User can search for an event by title and exclude unrelated cards
    When I open the Events page
    And I search events for "Dilli Diwali Mela"
    Then I should see event "Dilli Diwali Mela"
    And I should not see event "World Tech Summit"

  @regression @parallel-safe
  Scenario: User can filter events by city and category
    When I open the Events page
    And I filter events by category "Festival"
    And I filter events by city "Delhi"
    Then I should see event "Dilli Diwali Mela"

  @regression @parallel-safe
  Scenario: User can clear filters and restore the event list
    When I open the Events page
    And I filter events by category "Festival"
    And I filter events by city "Delhi"
    And I clear event filters
    Then I should see seeded upcoming events

  @regression @parallel-safe
  Scenario: User sees an empty state for unmatched search
    When I open the Events page
    And I search events for "No Selenium Event Should Match This"
    Then I should see the no events found message

  @regression @parallel-safe
  Scenario: Event cards expose business-critical details
    When I open the Events page
    Then event "Dilli Diwali Mela" card should show category "Festival", city "Delhi", price "$300", seats, and availability status

  @regression @parallel-safe
  Scenario Outline: User can open event details
    When I open details from <entryPoint> for event "<eventName>"
    Then I should see metadata for event "<eventName>"

    Examples:
      | entryPoint | eventName             |
      | the title  | World Tech Summit     |

  @regression @parallel-safe
  Scenario: User can open a bookable event detail page from Book Now
    When I open details from Book Now for a bookable event
    Then I should see the event detail booking panel
