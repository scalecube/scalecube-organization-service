@Configuration-service-production-ready

Feature: Organization service members management - Get members

  Organization Owner or Admin should be able to get the list of all existing members who steep into relevant organization.


  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-7679 (#92)
  Scenario: Successful get all the members list from relevant Organization by the "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority were invited to user's "A" organization both with a "member" role
    When the user "A" requested to get all members from own organization
    Then user "A" should receive successful response with list of members: "A", "B" and "C"


  #MPA-7679 (#93)
  Scenario: Successful get all the members list from relevant Organization by the the "admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to get all members from user's "A" organization
    Then user "B" should receive successful response with list of members: "A" and "B"


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-7679 (#94)
  Scenario: Fail to get the list of all the members from the relevant Organization by the existing member with similar role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "B" requested to get all members from user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#95)
  Scenario: Fail to get the list of all the members from the relevant Organization upon some of the existing (requester) managers was removed from the relevant organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    And the user "B" was removed from user's "A" organization
    When the user "B" requested to get the list of all the members from user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#96)
  Scenario: Fail to get members from non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And no organization were created yet
    When the user "A" requested to get all the members from non-existent organization "organizationId"
    Then user "A" should receive the error message: "Organization [id="organizationId"] not found"


  #MPA-7679 (#97)
  Scenario: Fail to get the user from specific Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When user "D" requested to get the list of all the members from some organization
    Then user "D" should receive the error message: "Token verification failed"
