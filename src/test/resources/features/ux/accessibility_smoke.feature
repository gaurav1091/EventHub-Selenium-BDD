@accessibility @ui @owner-platform @risk-ux @intent-accessibility
Feature: Accessibility smoke

  Background:
    Given I am signed in to EventHub

  @p1 @regression @parallel-safe
  Scenario Outline: Key pages expose basic accessibility semantics
    When I open UX smoke page "<page>"
    Then the page should expose basic accessibility semantics
    And the page should generate an Axe accessibility advisory report

    Examples:
      | page         |
      | home         |
      | events       |
      | bookings     |
      | admin events |
