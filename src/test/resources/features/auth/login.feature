@auth @ui @owner-platform @risk-auth @intent-auth @impact-auth
Feature: EventHub authentication

  @p0 @smoke @parallel-safe @critical @intent-session
  Scenario: Registered user can sign in and sign out
    Given I am on the EventHub login page
    When I sign in with valid registered credentials
    Then I should see the authenticated navigation
    When I sign out
    Then I should be returned to the login page

  @p1 @regression @parallel-safe @critical @intent-session
  Scenario: Authenticated session survives page refresh
    Given I am signed in to EventHub
    When I refresh the current page
    Then I should see the authenticated navigation

  @p1 @regression @parallel-safe @intent-validation
  Scenario: Login form validates required credentials
    Given I am on the EventHub login page
    When I submit the login form without credentials
    Then the login form should show required field validation

  @p1 @regression @parallel-safe @negative @intent-validation
  Scenario Outline: Invalid credentials are rejected
    Given I am on the EventHub login page
    When I sign in with password "<password>"
    Then I should see a login error

    Examples:
      | password          |
      | WrongPassword@123 |
      | Test@12345        |

  @p2 @regression @parallel-safe @intent-navigation
  Scenario: Register link opens account registration
    Given I am on the EventHub login page
    When I open the registration page from login
    Then I should be on the registration page

  @p0 @regression @parallel-safe @critical @intent-security
  Scenario: Anonymous user is redirected from protected route
    Given I am an anonymous visitor
    When I directly open protected route "/bookings"
    Then I should be returned to the login page

  @p0 @regression @parallel-safe @critical @intent-security
  Scenario: Anonymous user is redirected from the admin route
    Given I am an anonymous visitor
    When I directly open protected route "/admin/events"
    Then I should be returned to the login page

  @p1 @regression @parallel-safe @intent-session
  Scenario: Authenticated user can directly open a protected route
    Given I am signed in to EventHub
    When I directly open protected route "/bookings"
    Then the My Bookings page should be loaded

  @p1 @regression @parallel-safe @intent-session
  Scenario Outline: Authenticated session survives refresh on protected pages
    Given I am signed in to EventHub
    When I directly open protected route "<route>"
    And I refresh the current page
    Then protected page "<page>" should remain loaded

    Examples:
      | route         | page         |
      | /events       | events       |
      | /bookings     | bookings     |
      | /admin/events | admin events |

  @p1 @regression @parallel-safe @negative @intent-security @impact-events
  Scenario: Anonymous user is redirected from an event detail deep link
    Given I am an anonymous visitor
    When I directly open event detail for event "World Tech Summit"
    Then I should be returned to the login page
