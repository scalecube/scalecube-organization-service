@Configuration-service-production-ready

Feature: Organization service members management - Invite member

  Organization Owner or Admin should be able to invite authorized users to relevant organization.


  #__________________________________________________POSITIVE___________________________________________________________


  #MPA-7679 (#49)
  Scenario: Successful "member" invitation to multiple Organizations which belongs to different owners
    Given each of the users "A" and "B" have got a valid "token" issued by relevant authority
    And only two organizations "organizationId-1" and "organizationId-2" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority
    When each of the users "A" and "B" requested to invite the user "C" to step into own organization "organizationId-1" and "organizationId-2" as a "member"
    Then the user "C" should become the "member" in each from both organizations which belong to the users "A" and "B" accordingly
    And each of the users "A" and "B" should get successful response with the empty object


  #MPA-7679 (#50)
  Scenario: Successful invitation of specified member with "owner" role to multiple Organizations which belongs to single owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And several organizations "organizationId-3" and "organizationId-4" with specified "names" and "emails" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority
    When the user "A" requested to invite the user "B" to step into each own organizations "organizationId-3" and "organizationId-4" as an "owner"
    Then the user "B" should become the "owner" in each of both user's "A" organizations
    And user "A" should get successful responses with the empty object


  #MPA-7679 (#51)
  Scenario: Successful invitation of the "member" into specific Organization upon it's existent "member" was granted with "admin" role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite user "C" to step into user's "A" organization as a "member"
    Then the user "C" should become the "member" of user's "A" organization
    And user "B" should get successful response with the empty object


  #MPA-7679 (#52)
  Scenario: Successful invitation of specific member with "admin" role into relevant Organization upon it's existent "member" was granted with "owner" role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite user "C" to step into user's "A" organization with an "admin" role
    Then the user "C" should become the "admin" of user's "A" organization
    And user "B" should get successful response with the empty object


  #MPA-7679 (#53)
  Scenario: Fail to invite the existent "member" (duplicate) to the same Organization with the new role "Admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "A" requested to invite the existent user "B" to step into organization of user's "A" again with an "admin" role
    Then the user "B" shouldn't be duplicated as the existent member in the user's "A" organization
    And user "A" should get an error message:"user:'id@clients' already exists"


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-8170 (#54)
  Scenario: Fail to invite the user with invalid role to specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who have got the "userId" issued by relevant authority
    When the user "A" requested to invite the user "B" to step into organization of user's "A" with invalid "Boss" role
    Then user "A" should get an error message:"Role 'Boss' is invalid"


  #MPA-7679 (#55)
  Scenario: Fail to invite the user into relevant Organization upon the existing member (requester) got "member" role permission level
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite user "C" to step into user's "A" organization "organizationId" with a "member" role
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "B" name'"


  #MPA-7679 (#56)
  Scenario: Fail to invite the user into relevant Organization upon the existing owner (requester) was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    And the user "C" have got the "userId" issued by relevant authority
    And the user "A" was removed from own organization "organizationId"
    When the user "A" requested to invite user "C" to step into own former organization with a "member" role
    Then user "A" should get an error message: "user: 'userId "A"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#57)
  Scenario: Fail to invite the user as "Owner" into relevant Organization by the existing Admin (requester)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite user "C" to step into own former organization with an "owner" role
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', role: 'Admin' cannot invite to a higher role: 'Owner"


  #MPA-7679 (#58)
  Scenario: Fail to invite the user to specific Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When user "D" requested to invite some another user to step into some organization with some role
    Then user "D" should receive the error message: "Token verification failed"


  #MPA-8170 (#58.1)
  Scenario: Fail to invite the user to non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to invite some user to step into "non-existent" organizationId with some role
    Then user "A" should receive the error message: "Organization [id=non-existent] not found"
