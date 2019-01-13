 #KICK-OUT MEMBER - SHOULD WE RETURN INFO ABOUT REMOVED MEMBER INSTEAD OF PLAIN ACKNOWLEDGEMENT???

  #MPA-7229 (#6)
  Scenario: Successful remove (kick-out) of specific "member" from a specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" requested to remove the user "B" from user's "A" organization
    Then user "B" should abandon user's "A" organization and user "A" should get the empty object


  #MPA-7229 (#6.1) - SHOULD WE PROHIBIT TO REMOVE ORIGIN ORG OWNER BY ANOTHER OWNER/ADMIN EITHER EVENTUALLY DISABLE THIS ABILITY EVEN FOR ORIGIN OWNER???
  Scenario: Successful remove (kick-out) the owner of a specific Organization
    Given the user "A" have got a valid "token" and "userId" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to remove himself from user's "A" organization by its own decision
    Then user "A" should abandon own organization and should get the empty object


  #MPA-7229 (#6.2)
  Scenario: Successful remove (kick-out) of the "member" from specific Organization upon it's existent "member" was granted with "admin" role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user's "B" "member" role in the user's "A" organization was upgraded to "admin" role by the owner
    When the user "B" requested to remove user "C" from user's "A" organization "organizationId"
    Then user "C" should abandon user's "A" organization and user "B" should get the empty object


  #MPA-7229 (#6.3)
  Scenario: Successful remove (kick-out) of the "member" from specific Organization upon it's existent "member" was granted with "owner" role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user's "B" "member" role in the user's "A" organization was assigned to "owner" role by the owner
    When the user "B" requested to remove user "C" from user's "A" organization "organizationId"
    Then user "C" should abandon user's "A" organization and user "B" should get the empty object


  #MPA-7229 (#6.4) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful remove (kick-out) of specific "member" from specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to remove the user "B" from user's "A" organization "organizationId"
    Then user "B" should abandon the user's "A" organization and user "A" should get the empty object


  #MPA-7229 (#6.2)
  Scenario: Fail to remove the user from specific Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When user "D" requested to remove some user "userId" from some organization "organizationId"
    Then user "D" should receive the error message: "Token verification failed"


  #MPA-7229 (#6.3)
  Scenario: Fail to remove the a specific "member" from a specific Organization upon the host is not the member of the relevant Organization with appropriate permission level (role)
    Given each of the users "A", "B" and "C" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user "C" have got the "userId" issued by relevant authority
    When the user "C" applied own "token" and requested to remove the user "B" from user's "A" organization
    Then user "C" should get an error message: "user: 'userId "C"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#6.4)
  Scenario: Fail to remove the user from specific Organization upon the valid token have "member" role permission level
    Given each of the users "A", "B" and "C" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    When the user "B" requested to remove the user "C" from user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "B" name'"


  #MPA-7229 (#6.5)
  Scenario: Fail to remove the user from specific Organization upon this user is not a member (non-existent) of the relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    And the user "D" have got the "userId" issued by relevant authority
    When the user "A" requested to remove the user "D" from the user's "A" organization
    Then any existent "member" shouldn't be removed from the user's "A" organization and user "A" should get the empty object

