@Configuration-service-production-ready

Feature: Organization service API keys management - Add API key

  Organization Owner or Admin should be able to create the API keys for the relevant organization with appropriate
  permission level (members roles: Owner/Admin/Member) to be granted for Configuration service users.
  Thus owners could issue the API keys with all accessible roles but the admins are restricted by the "Admin" or "Member" role API keys issuing.


  #__________________________________________________POSITIVE___________________________________________________________
  

  #MPA-7603 (#35)
  Scenario: Successful adding the API keys (token) for relevant Organization with all accessible roles by Owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    Then each of the API keys with assigned roles: "owner", "admin" and "member" should be emitted for the relevant organization
    And the user "A" should get successful response with extended organization info which include all stored API keys
    And the user "A" requested the relevant organization to get stored API keys with extended organization info


  #MPA-7603 (#36)
  Scenario: Successful adding the API keys (token) with "admin" and "member" roles for relevant Organization by Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add the API key "name" for user's "A" organization with assigned role "owner"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to add the API keys "name" for user's "A" organization with assigned roles: "admin" and "member"
    Then each of the API keys with assigned roles: "admin" and "member" should be emitted for the relevant organization
    And the user "B" should get successful response with extended organization info which include only stored "admin" and "member" API keys
    And the user "A" requested the relevant organization to get stored API keys with extended organization info


  #__________________________________________________NEGATIVE___________________________________________________________


  #MPA-7603 (#37)
  Scenario: Fail to add the "owner" API key (token) for a relevant Organization by the Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    When the user "B" requested to add the API key "name" for user's "A" organization with assigned role "owner"
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner of organization: 'org "A" name'"


  #MPA-7603 (#38)
  Scenario: Fail to add some of accessible API keys (token) for a relevant Organization upon the relevant "member" doesn't manager's permission level
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "B" requested to add the API key "name" for user's "A" organization with assigned role "member"
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7603 (#39)
  Scenario: Fail to add some of accessible API keys (token) with the duplicate "name" for a relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" added API key "specifiedApiKeyName" for own organization with assigned role "owner"
    When the user "A" requested to add the API key with the existent "specifiedApiKeyName" for user's "A" organization assigned with any accessible role
    Then user "A" should receive the error message like:"apiKey name:'specifiedApiKeyName' already exists"


  #MPA-7603 (#40)
  Scenario: Fail to add some of accessible API keys (token) for relevant Organization upon the owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "A" was removed from user's "A" organization
    When the user "A" requested to add the API key "name" for former organization with assigned role "admin"
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7603 (#41)
  Scenario: Fail to add the API key (token) for a relevant Organization upon the assigned role is invalid (differs from allowed)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add the API key "name" for user's "A" organization assigned with invalid "role" like "boss"
    Then user "A" should receive the error message: "role": 'boss' is invalid"


  #MPA-7603 (#42)
  Scenario: Fail to add the API key (token) for a relevant Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to add some API key for some Organization
    Then this user should receive the error message: "Token verification failed"

