@Configuration-service-production-ready

Feature: Organization service Org management - Get Organization

  Organization members should be able to get extended info about relevant Organization.


  #__________________________________________________POSITIVE___________________________________________________________
  

  #MPA-7603 (#13)
  Scenario: Successful info get about relevant Organization by the Owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    When the user "B" requested to get the user's "A" organization info
    Then the user "B" should get successful response with extended organization info which include all stored API keys


  #MPA-7603 (#14) - the API key with "owner" role persisted but isn't shown to Admin
  Scenario: Successful info get about relevant Organization by the Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add the API keys "name" for own organization with assigned roles: "owner", "admin" and "member"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to get the user's "A" organization info
    And user "B" should get successful response with extended organization info which include only stored "admin" and "member" API keys


  #MPA-7603 (#15) - the API key with "owner" and "admin" roles persisted but isn't shown to Member
  Scenario: Successful info get about relevant Organization by the Member
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add the API keys "name" for own organization with assigned roles: owner", "admin" and "member"
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "C requested to get the user's "A" organization info
    And user "C" should get successful response with extended organization info which include only stored "member" API key


  #__________________________________________________NEGATIVE___________________________________________________________


  #MPA-7603 (#16)
  Scenario: Fail to get of specific Organization info upon the Owner was removed from relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "A" was removed from own organization
    When the user "A" requested to get own former organization info
    Then the user "A" should receive the error message: "user: 'null', name: 'userId "A"', is not a "member" of organization: 'user "A" organizationId'"


  #MPA-7603 (#17)
  Scenario: Fail to get a non-existent Organization info
    Given the user "A" have got a valid "token" issued by relevant authority
    And there is no organization "organizationId" was created and stored
    When the user "A" requested to get the "non-existent" organizationId info
    Then user "A" should receive the error message: "Organization [id=non-existent] not found"


  #MPA-7603 (#18)
  Scenario: Fail to get the Organization info if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to get a organization info with some "organizationId"
    Then this user should receive the error message: "Token verification failed"
