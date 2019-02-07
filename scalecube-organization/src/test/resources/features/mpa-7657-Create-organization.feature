@Configuration-service-production-ready

Feature: Creation of the Organization

  As a authorized user I would like to create my organization.


  #CREATE ORG

  #MPA-7229 (#1)
  Scenario: Successful creation of the Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to create the organization with specified non-existent "name" and some "email"
    Then user "A" should receive successful response with relevant organization details and relevant permission "owner"
    And "secret" for the relevant organization should be stored in Vault


  #MPA-7229 (#2)
  Scenario: Fail to create the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to create the organization with some "name" and "email"
    Then this user should get an error message: "Token verification failed"


  #MPA-7229 (#3)
  Scenario: Fail to create the Organization with the name which already exists (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to create the organization with the existent user's "B" organization "name" and some or the same "email"
    Then user "A" should get an error message: "Organization name: 'org "B" name' already in use"


  #MPA-7229 (#4)
  Scenario: Fail to create the Organization without email
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to create the organization with specified non-existent "name" but without "empty" email
    Then user "A" should get an error message: "Organization email cannot be empty"


  #MPA-7229 (#5)
  Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars
    Given the user "A" have got a valid "token" issued by relevant authority
    When the user "A" requested to create the organization with specified "name" which contains "+" and some "email"
    Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"
