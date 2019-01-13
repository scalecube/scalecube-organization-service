#DELETE ORG

  #MPA-7229 (#4)
  Scenario: Successful delete of specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to delete own organization "organizationId"
    Then user "A" should receive the successful response object: "deleted":true,"organizationId":"org "A" organizationId"


  #MPA-7229 (#4.1)
  Scenario: Successful delete of the Organization upon it's "member" was granted with admin role
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who had the "member" role in the user's "A" organization was assigned with "admin" role by the owner
    When the user "B" requested to delete user's "A" organization "organizationId"
    Then user "B" should receive the successful response object: "deleted":true,"organizationId":"org "A" organizationId"


  #MPA-7229 (#4.2)
  Scenario: Successful delete of the Organization upon it's "member" was granted with owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who had the "admin" role in the user's "A" organization was assigned with "owner" role by the owner
    When the user "B" requested to delete user's "A" organization "organizationId"
    Then user "B" should receive the successful response object: "deleted":true,"organizationId":"org "A" organizationId"


  #MPA-7229 (#4.3) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful delete a specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to delete user's "A" organization
    Then user "A" should receive the successful response object: "deleted":true,"organizationId":"org "A" organizationId"


  #MPA-7229 (#4.4)
  Scenario: Fail to delete the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to delete the organization with some "organizationId"
    Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#4.5)
  Scenario: Fail to delete the Organization upon the user is not the member of the relevant Organization with appropriate permission level (role)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any member already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to delete the user's "A" organization
    Then user "A" should receive an error message: "user: 'null', name: userId "B"', is not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#4.6)
  Scenario: Fail to delete the Organization upon the valid token have "member" role permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    When the user "B" applied own "token" and requested to delete the user's "A" organization with some "name" and some "email"
    Then the user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#4.7)
  Scenario: Fail to delete a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to delete the organization with non-existent "organizationId"
    Then user "A" should receive the error message: "organizationId" doesn't exist
