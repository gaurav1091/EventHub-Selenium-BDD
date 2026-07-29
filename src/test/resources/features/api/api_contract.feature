@api
Feature: EventHub API contract

  @smoke
  Scenario: API health endpoint is available
    When I request the API health endpoint
    Then the API health response should be successful

  @regression
  Scenario: Internal API contract manifest documents automated endpoints
    Then the internal API contract should document the automated endpoints

  @smoke
  Scenario: Registered user can authenticate through API
    When I authenticate through the API
    Then the API should return the registered user identity

  @regression
  Scenario: Authenticated user can retrieve current user profile through API
    When I request the current user profile through the API
    Then the API current user response should include the registered identity

  @smoke
  Scenario: Authenticated user can list events through API
    When I request events through the API
    Then the API events response should include seeded EventHub events

  @regression
  Scenario: Authenticated user can retrieve an event detail through API
    When I request event "World Tech Summit" through the API
    Then the API event detail response should describe "World Tech Summit"

  @smoke
  Scenario: Authenticated user can create and cancel a booking through API
    When I create a booking through the API
    Then the API booking response should include a booking reference
    When I cancel the API-created booking
    Then the API booking cancellation should be successful

  @regression @negative
  Scenario Outline: Protected API rejects invalid or anonymous requests
    When I <operation>
    Then the API should reject the request with status <status>

    Examples:
      | operation                                                 | status |
      | authenticate through the API with invalid credentials      | 400    |
      | request bookings through the API without authentication    | 401    |
      | create a booking through the API without authentication    | 401    |
      | request unknown event detail through the API               | 500    |
      | cancel an unknown booking through the API                  | 500    |
      | create a booking through the API with invalid payload      | 400    |
