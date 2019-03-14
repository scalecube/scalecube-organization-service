@Configuration-service-production-ready

Feature: Organization service members management - Kick-out member

  Organization Owner or Admin should be able to kick-out (remove) existing members from relevant organization.
  Nevertheless at least one organization owner should be persisted

  /**
  * #Note for the future implementation after the refactoring:
  * #Permission level for managers (Owner/Admin) API keys is automatically updated to the "member" one upon the managers who already issued these API keys
  * #were removed from the relevant organization.
  */


  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-7679 (#59)
  Scenario: Successful kick-out (remove) of specific "member" from a relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "A" requested to remove the user "B" from user's "A" organization
    Then user "B" should be removed from user's "A" organization
    And user "A" should get successful response with the empty object


  #MPA-7679 (#60)
  Scenario: Successful kick-out (remove) the "owner" and "member" from relevant Organization by another owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "B" requested to remove each of the users "A" and "C" from user's "A" organization
    Then users "A" and "C" should be removed from user's "A" organization
    And user "B" should get successful responses with the empty object


  #MPA-7679 (#61)
  Scenario: Successful kick-out (remove) of the "admin" and "member" from relevant Organization by another "admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority were invited to user's "A" organization both with "admin" role
    And the user "D" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "B" requested to remove each of the users "C" and "D" from user's "A" organization
    Then users "C" and "D" should be removed from user's "A" organization
    And user "B" should get successful responses with the empty object


  #MPA-7679 (#62)
  Scenario: Successful kick-out (remove) one of the "admin" from relevant Organization by "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to remove the user "C" from user's "A" organization
    Then user "C" should be removed from user's "A" organization
    And user "B" should get successful response with the empty object


  #MPA-7679 (#63)
  Scenario: Successful kick-out (remove) yourself as the "owner" from relevant Organization upon at least one another owner is persisted
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    When the user "A" requested to remove himself from own organization
    Then user "A" should be removed from user's "A" organization
    And user "A" should get successful response with the empty object


  #MPA-7679 (#64)
  Scenario: Successful kick-out (remove) yourself as the "admin" from relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to remove himself from user's "A" organization
    Then user "B" should be removed from user's "A" organization
    And user "B" should get successful response with the empty object


  /**
    * ##MPA-7679 (#65) - API key permission level is updated to "member" role upon the related manager (Owner) was removed from organization
    * #Scenario: Admin API key (token) for relevant Organization is downgraded to "member" one upon the issuer (Owner) of that API key was removed from Organization
    *  #Given the user "A" have got a valid "token" issued by relevant authority
    *  #And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    *  #And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    *  #And the user "B" requested to add the API key "name" for user's "A" organization with assigned role "owner"
    *  #When the user "B" was removed (kicked-out) from user's "A" organization
    *  #Then the API key with assigned role "owner" should be updated to "member"
    *  #And the relevant secret should be updated and stored in the Vault
    *  #And the API key with assigned role "member" should be persisted for the relevant organization
    *
    *
    * ##MPA-7679 (#65.a) - API key permission level is updated to "member" role upon the related manager (Admin) was removed from organization
    * #Scenario: Admin API key (token) for relevant Organization is downgraded to "member" one upon the issuer (Admin) of that API key was removed from Organization
    *  #Given the user "A" have got a valid "token" issued by relevant authority
    *  #And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    *  #And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    *  #And the user "B" requested to add the API key "name" for user's "A" organization with assigned role "admin"
    *  #When the user "B" was removed (kicked-out) from user's "A" organization
    *  #Then the API key with assigned role "admin" should be updated to "member"
    *  #And the relevant secret should be updated and stored in the Vault
    *  #And the API key with assigned role "member" should be persisted for the relevant organization
    */


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-7679 (#66)
  Scenario: Fail to kick-out non-existent "member" from some Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    When the user "A" requested to remove the non-existent user "non-existent-id@clients" from own organization
    Then user "A" should get an error message:"user:'id@clients' doesn't exist"


  #MPA-7679 (#67)
  Scenario: Fail to kick-out (remove) yourself as the single "owner" from relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to remove himself from own organization
    Then user "A" should get an error message: "At least one Owner should be persisted in the organization: 'organizationId'"


  #MPA-7679 (#68)
  Scenario: Fail to kick-out (remove) the single owner from relevant Organization by the "admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to remove the user "A" from user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', role: 'Admin' cannot kickout user: 'userId "A"' in role 'Owner' of organization: 'org "A" name'"


  #MPA-7679 (#69)
  Scenario: Fail to kick-out (remove) specific member from relevant Organization upon the existing member (requester) got "member" role permission level
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority were invited to user's "A" organization with a "member" role
    When the user "B" requested to remove the user "C" from user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#70)
  Scenario: Fail to remove a specific "member" from relevant Organization upon some of the existing (requester) managers was removed from the relevant organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "B" requested to remove the user "A" from user's "A" organization
    When the user "A" requested to remove the user "B" from user's "A" former organization
    Then user "A" should get an error message: "user: 'userId "A"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#71)
  Scenario: Fail to remove the user from specific Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When user "D" requested to remove some user from some organization
    Then user "D" should receive the error message: "Token verification failed"
