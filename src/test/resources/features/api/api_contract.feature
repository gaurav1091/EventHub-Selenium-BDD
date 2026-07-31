@api @contract @owner-platform @risk-contract @intent-api-contract @impact-api
Feature: EventHub API contract

  @p0 @smoke @parallel-safe @intent-health
  Scenario: API health endpoint is available
    When I request the API health endpoint
    Then the API health response should be successful

  @p1 @regression @parallel-safe @intent-documentation
  Scenario: Internal API contract manifest documents automated endpoints
    Then the internal API contract should document the automated endpoints

  @p0 @smoke @parallel-safe @intent-auth
  Scenario: Registered user can authenticate through API
    When I authenticate through the API
    Then the API should return the registered user identity
    And the API response should match schema "schemas/login-response.schema.json"

  @p1 @regression @parallel-safe @intent-auth
  Scenario: Authenticated user can retrieve current user profile through API
    When I request the current user profile through the API
    Then the API current user response should include the registered identity
    And the API response should match schema "schemas/current-user-response.schema.json"

  @p0 @smoke @parallel-safe @intent-discovery
  Scenario: Authenticated user can list events through API
    When I request events through the API
    Then the API events response should include seeded EventHub events
    And the API response should match schema "schemas/events-response.schema.json"

  @p1 @regression @parallel-safe @intent-discovery
  Scenario: Authenticated user can retrieve an event detail through API
    When I request event "World Tech Summit" through the API
    Then the API event detail response should describe "World Tech Summit"
    And the API response should match schema "schemas/event-detail-response.schema.json"

  @p0 @smoke @stateful @intent-booking
  Scenario: Authenticated user can create and cancel a booking through API
    When I create a booking through the API
    Then the API booking response should include a booking reference
    And the API response should match schema "schemas/booking-response.schema.json"
    When I cancel the API-created booking
    Then the API booking cancellation should be successful

  @p1 @regression @stateful @intent-booking
  Scenario: Booking cancellation is not silently repeatable
    When I create a booking through the API
    Then the API booking response should include a booking reference
    When I cancel the API-created booking
    Then the API booking cancellation should be successful
    When I cancel the API-created booking again
    Then the API should reject the request with an error response

  @p1 @regression @negative @parallel-safe @intent-security
  Scenario Outline: Protected API rejects invalid or anonymous requests
    When I <operation>
    Then the API should reject the request with status <status>

    Examples:
      | operation                                                 | status |
      | authenticate through the API with invalid credentials      | 400    |
      | request the current user profile through the API without authentication | 401    |
      | request bookings through the API without authentication    | 401    |
      | create a booking through the API without authentication    | 401    |
      | request unknown event detail through the API               | 500    |
      | cancel an unknown booking through the API                  | 500    |
      | create a booking through the API with invalid payload      | 400    |

  @p1 @regression @negative @parallel-safe @intent-contract
  Scenario Outline: API error responses follow the error contract
    When I <operation>
    Then the API should reject the request with an error response

    Examples:
      | operation                                                 |
      | authenticate through the API with invalid credentials      |
      | request bookings through the API without authentication    |
      | create a booking through the API with invalid payload      |
      | create an event through the API with invalid payload       |

  @p1 @regression @stateful @api-cleanup @intent-booking
  Scenario: Booking list maps to POJO responses after API booking creation
    When I create a booking through the API
    And I request bookings through the API
    Then the API bookings response should map to booking POJOs for the created booking

  @p1 @regression @stateful @api-cleanup @intent-booking
  Scenario: Duplicate booking attempts are handled consistently
    When I create the same booking through the API twice
    Then the duplicate booking response should follow the booking API contract

  @p1 @regression @stateful @api-cleanup @intent-capacity
  Scenario: Event capacity boundary rejects booking beyond available seats
    When I create a one-seat event through the API
    And I book the full API event capacity
    And I create one more booking for the capacity-bound event
    Then the API should reject the request with an error response
