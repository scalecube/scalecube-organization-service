@Configuration-service-production-ready

Feature: Creation of the Organization

  As a authorized user I would like to create my organization.


  #CREATE ORG

  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-7657 (#1)
  Scenario: Successful creation of the Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to create the organization with specified non-existent "name" and some "email"
    Then user "A" should receive successful response with relevant organization details and relevant permission "owner"
    And "secret" for the relevant organization should be stored in Vault


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-7657 (#2)
  Scenario: Fail to create the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to create the organization with some "name" and "email"
    Then this user should get an error message: "Token verification failed"


  #MPA-7657 (#3)
  Scenario: Fail to create the Organization with the name which already exists (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to create the organization with the existent user's "B" organization "name" and some or the same "email"
    Then user "A" should get an error message: "Organization name: 'org "B" name' already in use"


  #MPA-7657 (#4)
  Scenario: Fail to create the Organization without email either undefined email (i.e. null)
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to create the organization with following details
      | name  | email |
      | Org-1 |       |
      | Org-1 | null  |
    Then for each request user "A" should get an error message: "Please specify Organization email"


  #MPA-7657 (#5)
  Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars
    Given the user "A" have got a valid "token" issued by relevant authority
    When the user "A" requested to create the organization with specified "name" which contains "+" and some "email"
    Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"


  #MPA-7657 (#5.1)
  Scenario: Fail to create the Organization without name either undefined name (i.e. null)
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to create the organization with following details
      | name | email          |
      |      | my@email.com   |
      | null | some@email.com |
    Then for each request user "A" should get an error message: "Please specify Organization name"