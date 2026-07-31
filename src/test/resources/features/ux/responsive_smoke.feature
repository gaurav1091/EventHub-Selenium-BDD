@responsive @ui @owner-platform @risk-ux @intent-responsive
Feature: Responsive smoke

  Background:
    Given I am signed in to EventHub

  @p1 @regression @parallel-safe
  Scenario Outline: Key authenticated pages fit common viewport sizes
    When I use viewport <width> by <height>
    And I open UX smoke page "<page>"
    Then the page should fit the viewport without horizontal overflow

    Examples:
      | page         | width | height |
      | home         | 390   | 844    |
      | events       | 390   | 844    |
      | bookings     | 390   | 844    |
      | admin events | 1366  | 768    |
      | events       | 1440  | 900    |
