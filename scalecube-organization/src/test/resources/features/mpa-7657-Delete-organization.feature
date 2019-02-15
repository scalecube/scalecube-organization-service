@Configuration-service-production-ready

Feature: Delete of the Organization

  As a user I would like to delete my organization.


  #DELETE ORG

  #MPA-7657 (#1)
  Scenario: Successful delete of specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user "A" added the API keys to its organization with relevant roles "owner" and "admin"
    When the user "A" requested to delete own organization "organizationId"
    Then relevant organization with related API keys should be deleted
    And user "A" should receive the successful response: "deleted":true,"organizationId":"org "A" organizationId"
    And the relevant secret should be deleted from the Vault


  #MPA-7657 (#2)
  Scenario: Successful delete of the Organization upon it's "member" was granted with owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who had the "admin" role in the user's "A" organization was assigned with "owner" role by the owner
    When the user "B" requested to delete user's "A" organization "organizationId"
    Then relevant organization should be deleted
    And user "B" should receive the successful response: "deleted":true,"organizationId":"org "A" organizationId"
    And the relevant secret should be deleted from the Vault


  #MPA-7657 (#3)
  Scenario: Fail to delete a specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who had the "admin" role in the user's "A" organization was assigned with "owner" role by the owner
    And the user "A" was removed from user's "A" organization
    When the user "A" requested to delete user's "A" organization
    Then user "A" should receive the error message: "user: 'user Id "A"', not in role Owner of organization: 'org "A" name'"


  #MPA-7657 (#4)
  Scenario: Fail to delete the Organization upon it's "member" was granted with admin role permission level
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who had the "member" role in the user's "A" organization was assigned with "admin" role by the owner
    When the user "B" requested to delete user's "A" organization "organizationId"
    Then user "B" should receive the error message: "user: 'user Id "B"', not in role Owner of organization: 'org "A" name'"


  #MPA-7657 (#5)
  Scenario: Fail to delete the Organization upon the relevant member got the "member" role permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    When the user "B" requested to delete the user's "A" organization
    Then the user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner organization: 'org "A" name'"


  #MPA-7657 (#6)
  Scenario: Fail to delete the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to delete the organization with some "organizationId"
    Then this user should receive the error message: "Token verification failed"


  #MPA-7657 (#7)
  Scenario: Fail to delete a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to delete the organization with non-existent "organizationId"
    Then user "A" should receive the error message with non-existent: "organizationId"