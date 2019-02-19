@Configuration-service-production-ready

Feature: Organization service API keys management - Delete API key

  Organization Owner or Admin should be able to delete the API keys which were issued for the relevant organization.

  /**
  * #TBD:
  * #Organization Admins shouldn't be able to delete the API keys issued by the relevant Organization Owners.
  */

  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-7603 (#43)
  Scenario: Successful delete any of accessible API key (token) roles from relevant Organization by Owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    When the user "A" requested to delete each of the API keys "name" from own organization with assigned roles: "owner" and "admin"
    Then each of the API keys "name" with assigned roles: "owner" and "admin" should be deleted
    And the relevant secrets should be deleted from the DB
    And user "A" should get successful response with extended organization info which include the API key with "member" role only


  #MPA-7603 (#44) - the API key with "owner" role persisted but isn't shown to Admin
  Scenario: Successful delete the API keys (token) only with "admin" and "member" roles from relevant Organization by Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to delete the API keys "name" from user's "A" organization with assigned roles: "admin" and "member"
    Then each of the API keys "name" with assigned roles: "admin" and "member" should be deleted
    And the relevant secrets should be deleted from the Vault and DB
    And user "B" should get successful response with extended organization info which doesn't include any stored API key


  #__________________________________________________NEGATIVE___________________________________________________________

  /**
    *
    *##MPA-7603 (#45) - TBD if Admin could delete the API key assigned with "owner" role?
    *#Scenario: Fail to delete the API key (token) with "owner" role from relevant Organization by Admin
    *#  Given the user "A" have got a valid "token" issued by relevant authority
    *#  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    *#  And the user "A" requested to add the API key "name" for own organization with assigned role "owner"
    *#  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    *#  When the user "B" requested to delete the API key "name" from user's "A" organization with assigned role "owner"
    *#  Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner of organization: 'org "A" name'"
    */


  #MPA-7603 (#46)
  Scenario: Fail to delete any of accessible API key (token) roles from relevant Organization by the Member with similar role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add the API key "name" for own organization with assigned role "member"
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "member" role
    When the user "C" requested to delete the API key "name" from user's "A" organization with assigned role "member"
    Then user "C" should get an error message: "user: 'userId "C"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7603 (#47)
  Scenario: Fail to delete non-existent (invalid) API key (token) from specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add the API key "name" for own organization with assigned role "member"
    When the user "A" requested to delete the non-existent API key "someKey" from user's "A" organization
    Then user "A" should get successful response with extended organization info which include the API key with "member" role only


  #MPA-7603 (#48)
  Scenario: Fail to delete the API key (token) from relevant Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to delete some API key "name" in some Organization
    Then this user should receive the error message: "Token verification failed"

  

