@events @ui @owner-platform @risk-discovery @intent-discovery @impact-events
Feature: Event discovery

  Background:
    Given I am signed in to EventHub

  @p0 @smoke @parallel-safe @critical
  Scenario: User can view upcoming events
    When I open the Events page
    Then I should see seeded upcoming events

  @p1 @regression @parallel-safe
  Scenario Outline: User can search for events by keyword
    When I open the Events page
    And I search events for "<query>"
    Then I should see event "<expectedEvent>"

    Examples:
      | query              | expectedEvent      |
      | Dilli Diwali Mela  | Dilli Diwali Mela  |
      | Pragati Maidan     | Dilli Diwali Mela  |
      | World Tech Summit  | World Tech Summit  |

  @p1 @regression @parallel-safe
  Scenario: User can search for an event by title and exclude unrelated cards
    When I open the Events page
    And I search events for "Dilli Diwali Mela"
    Then I should see event "Dilli Diwali Mela"
    And I should not see event "World Tech Summit"

  @p1 @regression @parallel-safe
  Scenario: User can filter events by city and category
    When I open the Events page
    And I filter events by category "Festival"
    And I filter events by city "Delhi"
    Then I should see event "Dilli Diwali Mela"

  @p1 @regression @parallel-safe
  Scenario: User can clear filters and restore the event list
    When I open the Events page
    And I filter events by category "Festival"
    And I filter events by city "Delhi"
    And I clear event filters
    Then I should see seeded upcoming events

  @p1 @regression @parallel-safe
  Scenario: Event filters reset after refresh
    When I open the Events page
    And I search events for "Dilli Diwali Mela"
    And I refresh the current page
    Then event filters should be reset
    And I should see seeded upcoming events

  @p1 @regression @parallel-safe
  Scenario: Event filters reset after navigating away and back
    When I open the Events page
    And I search events for "Dilli Diwali Mela"
    And I filter events by city "Delhi"
    And I navigate away from event discovery and return
    Then event filters should be reset
    And I should see seeded upcoming events

  @p1 @regression @parallel-safe @negative
  Scenario: User sees an empty state for unmatched search
    When I open the Events page
    And I search events for "No Selenium Event Should Match This"
    Then I should see the no events found message

  @p1 @regression @parallel-safe @negative @intent-validation
  Scenario: Special-character event search does not show unrelated results
    When I open the Events page
    And I search events for "###@@@NoEvent"
    Then I should see no events for query "###@@@NoEvent"

  @p1 @regression @parallel-safe @negative @intent-filtering
  Scenario: Incompatible event filters show an empty result state
    When I open the Events page
    And I filter events by category "Festival"
    And I filter events by city "Hyderabad"
    Then I should see no matching events

  @p1 @regression @parallel-safe
  Scenario: Event cards expose business-critical details
    When I open the Events page
    Then event "Dilli Diwali Mela" card should show category "Festival", city "Delhi", price "$300", seats, and availability status

  @p1 @regression @parallel-safe
  Scenario Outline: User can open event details
    When I open details from <entryPoint> for event "<eventName>"
    Then I should see metadata for event "<eventName>"

    Examples:
      | entryPoint | eventName             |
      | the title  | World Tech Summit     |

  @p1 @regression @parallel-safe @critical
  Scenario: Event detail deep link loads correct metadata
    When I directly open event detail for event "World Tech Summit"
    Then I should see metadata for event "World Tech Summit"

  @p1 @regression @parallel-safe @critical @intent-navigation @impact-ux
  Scenario: Event detail remains stable after browser refresh
    When I directly open event detail for event "World Tech Summit"
    And I refresh the current page
    Then I should see metadata for event "World Tech Summit"

  @p1 @regression @parallel-safe @critical
  Scenario: User can open a bookable event detail page from Book Now
    When I open details from Book Now for a bookable event
    Then I should see the event detail booking panel

  @p1 @regression @parallel-safe @critical @intent-navigation @impact-ux
  Scenario: Browser back from event detail leaves the event journey usable
    When I open details from Book Now for a bookable event
    And I go back in the browser
    Then the event journey should remain usable
