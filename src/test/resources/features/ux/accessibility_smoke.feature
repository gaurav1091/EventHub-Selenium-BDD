@accessibility @ui
Feature: Accessibility smoke

  Background:
    Given I am signed in to EventHub

  @regression @parallel-safe
  Scenario Outline: Key pages expose basic accessibility semantics
    When I open UX smoke page "<page>"
    Then the page should expose basic accessibility semantics

    Examples:
      | page         |
      | home         |
      | events       |
      | bookings     |
      | admin events |
