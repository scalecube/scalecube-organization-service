#INVITE MEMBER  - SHOULD WE RETURN INFO ABOUT INVITED MEMBER INSTEAD OF PLAIN ACKNOWLEDGEMENT???

  #MPA-7229 (#5)
  Scenario: Successful "member" invitation to multiple Organizations which belongs to different owners
    Given each of the users "A" and "B" have got a valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" which don't contain any "member" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority
    When the user "A" requested to invite the user "C" to step into user's "A" organization "organizationId"
    And the user "B" requested to invite the user "C" to step into user's "B" organization "organizationId"
    Then the user "C" should become the "member" in each of both organizations which belong to the users "A" and "B" accordingly


  #MPA-7229 (#5.1)
  Scenario: Successful "member" invitation to multiple Organizations which belongs to single owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And several organizations "organizationId" with specified "name" and "email" which don't contain any "member" already created and owned by single user "A"
    And the user "B" have got the "userId" issued by relevant authority
    When the user "A" requested to invite the user "B" to step into each organization which belongs to user "A"
    Then the user "B" should become the "member" in each of both user's "A" organizations


  #MPA-7229 (#5.2)
  Scenario: Successful invitation of the "member" into specific Organization upon it's existent "member" was granted with admin role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by this user "A"
    And the user "B" who had the "member" role in the user's "A" organization was assigned with "admin" role by the owner
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite user "C" to step into user's "A" organization "organizationId"
    Then the user "C" should become the "member" of user's "A" organization


  #MPA-7229 (#5.3)
  Scenario: Successful invitation of the "member" into specific Organization upon it's existent "member" was granted with owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by this user "A"
    And the user "B" who had the "member" role in the user's "A" organization was assigned with "owner" role by the owner
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite user "C" to step into user's "A" organization "organizationId"
    Then the user "C" should become the "member" of user's "A" organization


  #MPA-7229 (#5.4) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful invitation of a "member" into specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "A" was removed from user's "A" organization by its own decision
    And the user "C" have got the "userId" issued by relevant
    When the user "A" requested to invite the user "C" to step into user's "A" organization "organizationId"
    Then the user "C" should become the "member" of user's "A" organization


  #MPA-7229 (#5.5) - SHOULD WE RETURN THE RELEVANT ERROR - USER ALREADY INVITED TO "ORG-ID" ???
  Scenario: Ignore to invite the existent "member" (duplicate) to the same Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" requested to invite the existent user "B" to step into organization of user's "A" again
    Then the user "B" shouldn't be duplicated as the existent "member" in the user's "A" organization and ignored by the system


  #MPA-7229 (#5.6)
  Scenario: Fail to invite the user to specific Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When user "D" requested to invite some user "userId" to some organization "organizationId"
    Then user "D" should receive the error message: "Token verification failed"


  #MPA-7229 (#5.7)
  Scenario: Fail to invite user into specific Organization upon the host is not the member of the relevant Organization with appropriate permission level (role)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority
    When the user "A" applied "token" of the user "B" and requested to invite the user "C" to step into user's "A" organization
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "B" name'"


  #MPA-7229 (#5.8)
  Scenario: Fail to invite the user into specific Organization upon the valid token have "member" role permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user "C" have got the "userId" issued by relevant authority
    When the user "B" requested to invite the user "C" to step into user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "B" name'"


  #MPA-7229 (#5.9) - SHOULD WE RETURN THE RELEVANT ERROR - USER AUTHENTICATION IS FAILED ???
  Scenario: Fail to invite the user to specific Organization upon this user is unauthorized (invalid or non-existent)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "D" have got the invalid "userId"
    When the user "A" requested to invite the user "D" to step into user's "A" organization
    Then user "D" shouldn't become the "member" of the user's "A" organization and user "A" should get the empty object



