@auth @ui
Feature: EventHub authentication

  @smoke
  Scenario: Registered user can sign in and sign out
    Given I am on the EventHub login page
    When I sign in with valid registered credentials
    Then I should see the authenticated navigation
    When I sign out
    Then I should be returned to the login page

  @regression
  Scenario: Login form validates required credentials
    Given I am on the EventHub login page
    When I submit the login form without credentials
    Then the login form should show required field validation

  @regression
  Scenario Outline: Invalid credentials are rejected
    Given I am on the EventHub login page
    When I sign in with password "<password>"
    Then I should see a login error

    Examples:
      | password          |
      | WrongPassword@123 |
      | Test@12345        |

  @regression
  Scenario: Register link opens account registration
    Given I am on the EventHub login page
    When I open the registration page from login
    Then I should be on the registration page

  @regression
  Scenario: Anonymous user is redirected from protected route
    Given I am an anonymous visitor
    When I directly open protected route "/bookings"
    Then I should be returned to the login page
