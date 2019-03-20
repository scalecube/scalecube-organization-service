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
    And user "A" should get successful response with extended organization info which include the API key with "member" role only
    And the user "A" requested the relevant organization to get remaining API keys with extended organization info


  #MPA-7603 (#44) - the API key with "owner" role persisted but isn't shown to Admin
  Scenario: Successful delete the API keys (token) only with "admin" and "member" roles from relevant Organization by Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to delete the API keys "name" from user's "A" organization with assigned roles: "admin" and "member"
    Then each of the API keys "name" with assigned roles: "admin" and "member" should be deleted
    And user "B" should get successful response with extended organization info which doesn't include any stored API key
    And the user "A" requested the relevant organization to get remaining API keys with extended organization info


  #__________________________________________________NEGATIVE___________________________________________________________

  /**
    *
    *##MPA-7603 (#44.a) - TBD if Admin could delete the API key assigned with "owner" role?
    *#Scenario: Fail to delete the API key (token) with "owner" role from relevant Organization by Admin
    *#  Given the user "A" have got a valid "token" issued by relevant authority
    *#  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    *#  And the user "A" requested to add the API key "name" for own organization with assigned role "owner"
    *#  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    *#  When the user "B" requested to delete the API key "name" from user's "A" organization with assigned role "owner"
    *#  Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner of organization: 'org "A" name'"
    */

  #MPA-8170 (#45)
  Scenario: Fail to delete some of accessible API keys (token) from relevant Organization upon the owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" added API key "specifiedApiKeyName" for own organization with assigned role "owner"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "A" was removed from user's "A" organization
    When the user "A" requested to delete the API key "specifiedApiKeyName" from his former organization
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


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
    When the user "A" requested to delete the non-existent API key "someKey" name from user's "A" organization
    Then user "A" should receive the error message: "Key 'someKey' doesn't exist"


  #MPA-7603 (#48)
  Scenario: Fail to delete the API key (token) from relevant Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to delete some API key "name" in some Organization
    Then this user should receive the error message: "Token verification failed"


  #MPA-8170 (#48.1)
  Scenario: Fail to delete the API key (token) from non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    When the user "A" requested to delete some API key "name" from "non-existent" organizationId
    Then user "A" should receive the error message: "Organization [id=non-existent] not found"

  

