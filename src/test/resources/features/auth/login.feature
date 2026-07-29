@auth @ui
Feature: EventHub authentication

  @smoke @parallel-safe
  Scenario: Registered user can sign in and sign out
    Given I am on the EventHub login page
    When I sign in with valid registered credentials
    Then I should see the authenticated navigation
    When I sign out
    Then I should be returned to the login page

  @regression @parallel-safe
  Scenario: Authenticated session survives page refresh
    Given I am signed in to EventHub
    When I refresh the current page
    Then I should see the authenticated navigation

  @regression @parallel-safe
  Scenario: Login form validates required credentials
    Given I am on the EventHub login page
    When I submit the login form without credentials
    Then the login form should show required field validation

  @regression @parallel-safe
  Scenario Outline: Invalid credentials are rejected
    Given I am on the EventHub login page
    When I sign in with password "<password>"
    Then I should see a login error

    Examples:
      | password          |
      | WrongPassword@123 |
      | Test@12345        |

  @regression @parallel-safe
  Scenario: Register link opens account registration
    Given I am on the EventHub login page
    When I open the registration page from login
    Then I should be on the registration page

  @regression @parallel-safe
  Scenario: Anonymous user is redirected from protected route
    Given I am an anonymous visitor
    When I directly open protected route "/bookings"
    Then I should be returned to the login page
