@visual @ui @owner-platform @risk-ux @intent-visual @impact-ux
Feature: Visual sanity smoke

  @p2 @regression @parallel-safe
  Scenario: Login page visual sanity screenshot is captured
    When I open UX smoke page "login"
    Then the page should have a visual sanity screenshot

  @p2 @regression @parallel-safe
  Scenario Outline: Authenticated key pages have visual sanity screenshots
    Given I am signed in to EventHub
    When I open UX smoke page "<page>"
    Then the page should have a visual sanity screenshot

    Examples:
      | page         |
      | events       |
      | bookings     |
      | admin events |
