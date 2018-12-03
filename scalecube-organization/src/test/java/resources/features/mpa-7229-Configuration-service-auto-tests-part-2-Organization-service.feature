@Configuration-service-production-ready

Feature: Basic CRUD tests for organization service.

  As a user I would like to create and manage my organization.

  For example:

  - add new organization, update it, be sure the changes are saved inquiring the current state and be able to delete my organization
  - invite the members to my organization and remove each member out from it
  - get know all the members steeped in my organization
  - leave the organization as former member and know if still got the membership in any of the other organization
  - grant the permission level (key) for members according to relevant role and change this permission level or even delete it.


  #_____________________________________________________CRUD____________________________________________________________

  #MPA-7229 (#1)
  Scenario: Successful creation of the Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    When this user requested to create the organization with specified "name" and "email"
    Then new organization should be created and stored in DB with relevant "organizationId" assigned for potential members


  #MPA-7229 (#1.1)
  Scenario: Fail to create the Organization if the token is invalid (expired)
    Given a user have got the invalid "token"
    When this user requested to create the organization with specified "name" and "email"
    Then new organization shouldn't be created and the user should get an error message: "Token verification failed"


  #MPA-7229 (#1.2)
  Scenario: Fail to create the Organization with the name which already exists (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to create the organization with the existent user's "B" organization "name" and some or the same "email"
    Then new organization shouldn't be created and the user "A" should get an error message: "name": organization already exists"




  #MPA-7229 (#2)
  Scenario: Successful update of the Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    When this user requested to update the organization with some non-existent "name" and some or the same "email"
    Then user "A" should receive the successful response with updated name and email of the relevant organization


  #MPA-7229 (#2.1)
  Scenario: Fail to update the Organization upon the valid token belongs to another Organization owner
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "B" requested to update the user's "A" organization with some "name" and some "email"
    Then data in organization owned by user "A" shouldn't be changed and the user "B" should get an error message: "not the owner"


  #MPA-7229 (#2.2)
  Scenario: Fail to update the Organization with the name which already exists (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to update the organization with the existent user's "B" organization "name" and some or the same "email"
    Then new organization shouldn't be created and the user "A" should get an error message: "name": organization already exists"


  #MPA-7229 (#2.3)
  Scenario: Fail to update the non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to update the organization with non-existent "name" and some "email"
    Then any of stored organizations data shouldn't be changed and the user "A" should get the empty object



  #MPA-7229 (#3)
  Scenario: Successful get info about specific Organization from the system
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to get the own organization info
    Then user "A" should receive the successful response with relevant organization data


  #MPA-7229 (#3.1)
  Scenario: Fail to get the Organization info upon the valid token belongs to another Organization owner
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "B" requested to get the user's "A" organization info
    Then the user "B" shouldn't get any organization info and receive an error message: "not the owner"


  #MPA-7229 (#3.2)
  Scenario: Fail to get a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to update the organization with non-existent "organizationId"
    Then user "A" shouldn't get any of stored organizations data and receive the empty object



  #MPA-7229 (#4)
  Scenario: Successful delete of specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to delete the own organization
    Then user "A" should receive the successful response with empty object


  #MPA-7229 (#4.1)
  Scenario: Fail to delete the Organization upon the valid token belongs to another Organization owner
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "B" requested to delete the user's "A" organization
    Then any of stored organizations shouldn't be deleted and user "B" should receive an error message: "not the owner"


  #MPA-7229 (#4.2)
  Scenario: Fail to delete a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to delete the organization with non-existent "organizationId"
    Then any of stored organizations shouldn't be deleted and user "A" should receive receive the empty object



  #MPA-7229 (#5)
  Scenario: Successful member invitation to specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority
    When the user "A" requested to invite the user "B" to step into user's "A" organization
    Then user "B" should become the member of the user's "A" organization


  #MPA-7229 (#5.1)
  Scenario: Fail to invite the user to specific Organization upon the valid token belongs to another Organization owner
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority
    When the user "A" applied "token" of the user "B" and requested to invite the user "C" to step into user's "A" organization
    Then user "C" shouldn't become the member of the user's "A" organization and user "A" should get an error message: "not the owner"
  

  #MPA-7229 (#5.2)
  Scenario: Fail to invite the user to foreign Organization
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority
    When the user "A" applied own "token" and requested to invite the user "C" to step into user's "B" organization "organizationId"
    Then user "C" shouldn't become the member of the user's "B" organization and user "A" should get an error message: "not the owner"


  #MPA-7229 (#5.3)
  Scenario: Fail to invite the user to specific Organization upon the user is unauthorized (invalid or non-existent)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "D" have got the invalid "userId"
    When the user "A" requested to invite the user "C" to step into user's "A" organization
    Then user "D" shouldn't become the member of the user's "A" organization and user "A" should get the empty object



  #MPA-7229 (#6)
  Scenario: Successful remove (kick-out) a specific member from a specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the member of the user's "A" organization
    When the user "A" requested to remove the user "B" from user's "A" organization
    Then user "B" should abandon user's "A" organization and user "A" should get the empty object


  #MPA-7229 (#6.1)
  Scenario: Fail to remove the a specific member from a specific Organization upon the valid token belongs to another Organization owner
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority and became the member of the user's "A" organization
    When the user "A" applied "token" of the user "B" and requested to remove the user "C" from the user's "A" organization
    Then user "C" shouldn't abandon user's "A" organization and user "A" should get an error message: "not the owner"


  #MPA-7229 (#6.2)
  Scenario: Fail to remove the user from foreign Organization
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority and became the member of the user's "B" organization
    When the user "A" applied own "token" and requested to remove the user "C" from user's "B" organization "organizationId"
    Then user "C" shouldn't abandon user's "B" organization and user "A" should get an error message: "not the owner"


  #MPA-7229 (#6.3)
  Scenario: Fail to remove the user from specific Organization upon the user is unauthorized (invalid or non-existent)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the member of the user's "A" organization
    And the user "D" have got the invalid "userId"
    When the user "A" requested to remove the user "D" from the user's "A" organization
    Then any member shouldn't be removed from the user's "A" organization and user "A" should get the empty object




