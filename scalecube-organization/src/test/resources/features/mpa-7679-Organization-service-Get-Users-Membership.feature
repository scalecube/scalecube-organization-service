@Configuration-service-production-ready

Feature: Organization service Members management - Get members membership

  Any member should be able to know own membership in each relevant Organization the member was invited (steep in).



  #__________________________________________________POSITIVE___________________________________________________________
  

  #MPA-7679 (#98)
  Scenario: Successful get the list of all Organizations (Membership) in each the user became a Member
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And organization "organizationId-1" with specified "name-1" and "email" already created and owned by user "A"
    And organization "organizationId-2" with specified "name-2" and "email" already created and owned by user "B"
    And the user "A" requested to add each accessible API key "name" for own organization "organizationId-1" with assigned roles: "owner" and "admin"
    And the user "B" requested to add each accessible API key "name" for own organization "organizationId-2" with assigned roles: "admin" and "member"
    And the user "C" who have got the "userId" issued by relevant authority was invited to each of both organizations: users' "A" and "B" with a "member" role
    When the user "C" requested to get own Membership
    Then the user "C" should receive successful response with list of all the Organizations which belong to users "A" and "B"
    And the extended info for user's "A" organization "organizationId-1" shouldn't include any of the stored API key
    And the extended info for user's "B" organization "organizationId-2" should include only stored "member" API key


  #MPA-7679 (#99)
  Scenario: Successful get the list of all Organizations (Membership) in each the user became an Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And organization "organizationId-3" with specified "name-3" and "email" already created and owned by user "A"
    And organization "organizationId-4" with specified "name-4" and "email" already created and owned by user "A" also
    And the user "A" requested to add each accessible API key "name" for own organization "organizationId-3" with assigned roles: "owner" and "admin"
    And the user "A" requested to add each accessible API key "name" for another own organization "organizationId-4" with assigned roles: "admin" and "member"
    And the user "D" who have got the "userId" issued by relevant authority was invited to each of both users' "A" organizations with an "admin" role
    When the user "D" requested to get own Membership
    Then the user "D" should receive successful response with list of all the Organizations which belong to user "A"
    And the extended info for user's "A" organization "organizationId-3" should include only stored "admin" API key
    And the extended info for user's "A" organization "organizationId-4" should include only stored "admin" and "member" API keys


  #MPA-7679 (#100)
  Scenario: Successful get the list of all Organizations (Membership) in each the user became an Owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization "organizationId-5" with assigned roles: "owner", "admin" and "member"
    And the user "B" who have got the "userId" issued by relevant authority was invited to the users' "A" organization with an "owner" role
    When the user "B" requested to get own Membership
    Then the user "B" should receive successful response with list of all the Organizations which belong to user "A"
    And the extended info for user's "A" organization "organizationId" should include all stored API keys


  #MPA-7679 (#101)
  Scenario: Do not get any Organization data upon the user hasn't became a member (wasn't invited) to any of the relevant Organizations
    Given the user "A" have got a valid "token" issued by relevant authority
    And organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority but wasn't invited to any of the existent organizations
    When the user "C" requested to get the Membership
    Then user "C" should receive successful response with empty object


  #__________________________________________________NEGATIVE___________________________________________________________


  #MPA-7679 (#102)
  Scenario: Fail to get the Membership in Organizations upon the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to get own membership from some organization
    Then this should receive the error message: "Token verification failed"

