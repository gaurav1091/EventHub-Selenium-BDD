@api
Feature: EventHub API contract

  @smoke @parallel-safe
  Scenario: API health endpoint is available
    When I request the API health endpoint
    Then the API health response should be successful

  @regression @parallel-safe
  Scenario: Internal API contract manifest documents automated endpoints
    Then the internal API contract should document the automated endpoints

  @smoke @parallel-safe
  Scenario: Registered user can authenticate through API
    When I authenticate through the API
    Then the API should return the registered user identity
    And the API response should match schema "schemas/login-response.schema.json"

  @regression @parallel-safe
  Scenario: Authenticated user can retrieve current user profile through API
    When I request the current user profile through the API
    Then the API current user response should include the registered identity
    And the API response should match schema "schemas/current-user-response.schema.json"

  @smoke @parallel-safe
  Scenario: Authenticated user can list events through API
    When I request events through the API
    Then the API events response should include seeded EventHub events
    And the API response should match schema "schemas/events-response.schema.json"

  @regression @parallel-safe
  Scenario: Authenticated user can retrieve an event detail through API
    When I request event "World Tech Summit" through the API
    Then the API event detail response should describe "World Tech Summit"
    And the API response should match schema "schemas/event-detail-response.schema.json"

  @smoke @stateful
  Scenario: Authenticated user can create and cancel a booking through API
    When I create a booking through the API
    Then the API booking response should include a booking reference
    And the API response should match schema "schemas/booking-response.schema.json"
    When I cancel the API-created booking
    Then the API booking cancellation should be successful

  @regression @negative @parallel-safe
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
