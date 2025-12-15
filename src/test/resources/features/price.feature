Feature: Price consultation

  Background:
    Given the PRICES table is reset with sample data
    And the product with id 35455 and brand 1

  Scenario: Test 1 - 14 June 10:00
    When I request the price at "14/06/2020 10:00:00"
    Then the response contains price 35.50

  Scenario: Test 2 - 14 June 16:00
    When I request the price at "14/06/2020 16:00:00"
    Then the response contains price 25.45

  Scenario: Test 3 - 14 June 21:00
    When I request the price at "14/06/2020 21:00:00"
    Then the response contains price 35.50

  Scenario: Test 4 - 15 June 10:00
    When I request the price at "15/06/2020 10:00:00"
    Then the response contains price 30.50

  Scenario: Test 5 - 16 June 21:00
    When I request the price at "16/06/2020 21:00:00"
    Then the response contains price 38.95
