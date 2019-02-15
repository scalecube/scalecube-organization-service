@Configuration-service-production-ready

Feature: Organization service Org management - Update Organization

  Organization managers should be able to update relevant Organization.


  #__________________________________________________POSITIVE___________________________________________________________
  

  #MPA-7603 (#19)
  Scenario: Successful update of the relevant Organization by the Owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    When the user "A" requested to update own organization with some non-existent "name" and some or the existent "email"
    Then the user "A" should get successful response with extended organization info which include all stored API keys


  #MPA-7603 (#20)
  Scenario: Successful update of the relevant Organization by the Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to update user's "A" organization with some non-existent "name" and some or the existent "email"
    Then the user "B" should get successful response with extended organization info which include only stored "admin" and "member" API keys


  #MPA-7603 (#21)
  Scenario: Successful update of the Organization upon it's "member" was granted with Owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    And the user "A" requested to update the user "C" role to "owner" in the own organization
    When the user "C" requested to update user's "A" organization with some non-existent "name" and some or the existent "email"
    Then the user "C" should get successful response with extended organization info without any stored API key




  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-7603 (#22)
  Scenario: Fail to update relevant Organization by the Member with similar role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "C" requested to update user's "A" organization with some non-existent "name" and some or the existent "email"
    Then the user "C" should get an error message: "user: 'userId "C"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7603 (#23)
  Scenario: Fail to update relevant Organization upon the Owner was removed from it
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "A" was removed from own organization
    When the user "A" requested to update own former organization with some non-existent "name" and some or the existent "email"
    Then the user "A" should get an error message: "user: 'userId "A"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7603 (#24)
  Scenario: Fail to update the Organization with the name which already exists (duplicate)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And organization "organizationId" with specified "name-1" and "email" already created and owned by user "A"
    And organization "organizationId" with specified "name-2" and "email" already created and owned by user "B"
    When the user "A" requested to update own organization "name-1" with the existent user's "B" organization "name-2"
    Then user "A" should get an error message: "Organization name: 'org "B" name' already in use"


  #MPA-7603 (#25)
  Scenario: Fail to update the non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And there is no organization "organizationId" was created and stored
    When the user "A" requested to update the non-existent organization "organizationId" with some "name" or "email"
    Then user "A" should receive the error message with non-existent: "organizationId"


  #MPA-7603 (#26)
  Scenario: Fail to update the Organization with the name which contain else symbols apart of allowed chars
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to update own organization with specified "name" which contains "+" and some "email"
    Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"


  #MPA-7603 (#27)
  Scenario: Fail to update the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to update some organization "organizationId" with some "name" and "email"
    Then this user should get an error message: "Token verification failed"

