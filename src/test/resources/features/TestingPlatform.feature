Feature: Basic Testing Integration Check
  As a tester
  I want to verify that Cucumber steps are correctly linked
  So that I can confirm the integration is working

  Scenario: Successful Cucumber Integration Check
    Given the application is running
    When I perform a simple check
    Then the result should be success