@Configuration-service-production-ready

Feature: Basic CRUD tests for organization service.

  As a user I would like to create and manage my organization.

  For example:

  - add new organization, update it, be sure the changes are saved inquiring the current state and be able to delete my organization
  - invite the members to my organization and remove each "member" out from it
  - get know all the members steeped in my organization
  - leave the organization as former "member" and know if still got the membership in any of the other organization
  - grant the permission level (key) for members according to relevant role and change this permission level or even delete it.



  #CREATE ORG

  #MPA-7229 (#1)
  Scenario: Successful creation of the Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    When user "A" requested to create the organization with specified non-existent "name" and some "email"
    Then new organization should be created and stored in DB with relevant "organizationId" assigned for potential members
    And the user "A" should become the "owner" among the potential members of the relevant organization


  #MPA-7229 (#1.1)
  Scenario: Fail to create the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to create the organization with some "name" and "email"
    Then this user should get an error message: "Token verification failed"


  #MPA-7229 (#1.2)
  Scenario: Fail to create the Organization with the name which already exists (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "B"
    When the user "A" requested to create the organization with the existent user's "B" organization "name" and some or the same "email"
    Then user "A" should get an error message: "Organization name: 'org "B" name' already in use"


  #MPA-7229 (#1.3) - SHOULD WE REMOVE SUCH VALIDATION AND ENABLE TO ADD ANY CHARS?
  Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars
    Given the user "A" have got a valid "token" issued by relevant authority
    When the user "A" requested to create the organization with specified "name" which contains "+" and some "email"
    Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"



  #GET ORG

  #MPA-7229 (#2)
  Scenario: Successful info get about specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to get the own organization info
    Then user "A" should receive the successful response with relevant organization data


  #MPA-7229 (#2.1) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful get of specific Organization info upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to get user's "A" organization info
    Then user "A" should receive the successful response with relevant organization data


  #MPA-7229 (#2.2)
  Scenario: Successful get of specific Organization info by some of the Organization members with any of accessible role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    When the user "B" requested to get user's "A" organization info
    Then user "B" should receive the successful response with relevant organization data


  #MPA-7229 (#2.3)
  Scenario: Fail to get the Organization info if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to get a organization info with some "organizationId"
    Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#2.4)
  Scenario: Fail to get the Organization info upon the user is not the member of this Org
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to get the user's "A" organization info
    Then the user "A" should receive the error message: "user: 'null', name: 'userId "B"', is not a "member" of organization: 'user "A" organizationId'"


  #MPA-7229 (#2.5)
  Scenario: Fail to get a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And there is no organization "organizationId" was created and stored
    When the user "A" requested to get the non-existent organization "organizationId" info
    Then user "A" should receive the error message: "organizationId" doesn't exist



  #UPDATE ORG

  #MPA-7229 (#3)
  Scenario: Successful update of the Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    When user "A" requested to update own organization with some non-existent "name" and some or the existent "email"
    Then user "A" should receive the successful response with relevant organization updated data of "name" and "email"


  #MPA-7229 (#3.1)
  Scenario: Successful update of the Organization upon it's "member" was granted with admin role
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user "B" who had the "member" role in the user's "A" organization was assigned with "admin" role by the owner
    When the user "B" requested to update user's "A" organization with some non-existent "name" and some or the existent "email"
    Then user "B" should receive the successful response with relevant organization updated data of "name" and "email"


  #MPA-7229 (#3.2)
  Scenario: Successful update of the Organization upon it's "member" was granted with owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And the users "B" and "C" who have got the "userId" issued by relevant authority became the "admin" and "member" accordingly in the user's "A" organization
    And the user "C" who had the "member" role in the user's "A" organization was assigned with "owner" role by the user "B" "admin" permission
    When the user "C" requested to update user's "A" organization with some non-existent "name" and some or the existent "email"
    Then user "C" should receive the successful response with relevant organization updated data of "name" and "email"


  #MPA-7229 (#3.3) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful update of specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to update user's "A" organization with some non-existent "name" and some or the existent "email"
    Then user "A" should receive the successful response with relevant organization updated data of "name" and "email"


  #MPA-7229 (#3.4)
  Scenario: Fail to update the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to update the organization with some "name" and "email"
    Then this user should get an error message: "Token verification failed"


  #MPA-7229 (#3.5)
  Scenario: Fail to update the Organization upon the user is not the member of this Org
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to update the user's "A" organization with some "name" and some "email"
    Then the user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#3.6)
  Scenario: Fail to update the Organization upon the valid token have "member" role permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    When the user "B" applied own "token" and requested to update the user's "A" organization with some "name" and some "email"
    Then the user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#3.7)
  Scenario: Fail to update the non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to update the organization with non-existent "organizationId" and some "name" and "email"
    Then user "A" should receive the error message: "organizationId" doesn't exist


  #MPA-7229 (#3.8)
  Scenario: Fail to update the Organization with the name which already exists (duplicate)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    When the user "A" requested to update own organization "name" with the existent user's "B" organization "name" and some or the same "email"
    Then user "A" should get an error message: "Organization name: 'org "B" name' already in use"


  #MPA-7229 (#3.9) - SHOULD WE REMOVE SUCH VALIDATION AND ENABLE TO ADD ANY CHARS?
  Scenario: Fail to update the Organization with the name which contain else symbols apart of allowed chars
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to update the organization with specified "name" which contains "+" and some "email"
    Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"



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



  #GET MEMBER FROM ORG

  #MPA-7229 (#7)
  Scenario: Successful get the list of all the members from the specific Organization (Members in Organizations) by the origin owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" have got the "userId" issued by relevant authority and both became the members of the user's "A" organization
    When the user "A" requested to get all members from own organization
    Then user "A" should receive successful response with list of all the members of the own organization i.e. users "B" and "C" including the owner "A"


 #MPA-7229 (#7.1)
  Scenario: Successful get the list of all the members from the specific Organization (Members in Organizations) by some member from the relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" have got the "userId" issued by relevant authority and both became the members of the user's "A" organization
    When the user "B" requested to get all members from user's "A" organization
    Then user "B" should receive successful response with list of all the members of the own organization i.e. users "B" and "C" including the owner "A"


  #MPA-7229 (#7.2) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful get the list of all the members from the specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to get the list of all the members from own organization
    Then user "A" should receive successful response with list of all the members of the own organization i.e. user "B" only


  #MPA-7229 (#7.3)
  Scenario: Fail to get the user from specific Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When user "D" requested to get some user from some organization "organizationId"
    Then user "D" should receive the error message: "Token verification failed"


  #MPA-7229 (#7.4) - SHOULD WE RETURNS MESSAGE - NO MEMBERS WERE FOUND???
  Scenario: Do not get any "member" from a specific Organization if nobody steeped in
    Given the user "A" have got a valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "A" requested to get all members from own organization
    Then user "A" should receive successful response with empty object


  #MPA-7229 (#7.6)
  Scenario: Fail to get the members from a specific Organization upon the host is not the member of the relevant Organization
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" applied "token" of the user "B" and requested to get all the members from the user's "A" organization
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', is not a "member" of organization: 'user "A" organizationId'"


  #MPA-7229 (#7.7)
  Scenario: Fail to get members from non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to get all the members from non-existent organization "organizationId"
    Then user "A" should receive the error message: "organizationId" doesn't exist



  #GET MEMBERSHIP

  #MPA-7229 (#8)
  Scenario: Successful get the list of all Organizations in each the user became a "member" (Membership in Organizations)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only two organizations "organizationId" with specified "name" and "email" already created and owned by each of the user's "A" and "B"
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the users' "A" and "B" organizations
    When the user "C" requested to get the Membership
    Then user "C" should receive successful response with list of all the Organizations i.e. "A" and "B" in which user "C" was invited (became a member with accessible role)


  #MPA-7229 (#8.1) - SHOULD WE RETURNS MESSAGE - NO ORGANIZATIONS WERE FOUND???
  Scenario: Do not get any Organization data upon the user hasn't became a member (wasn't invited) to any of the relevant Organizations
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority
    When the user "C" requested to get the Membership
    Then user "C" should receive successful response with empty object


  #MPA-7229 (#8.2)
  Scenario: Fail to get the Membership in Organizations upon the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to get membership from some organization
    Then this should receive the error message: "Token verification failed"



  #LEAVE ORG - SHOULD WE RETURN INFO ABOUT MEMBER WHO LEAVED THE ORGANIZATION INSTEAD OF PLAIN ACKNOWLEDGEMENT???

  #MPA-7229 (#9)
  Scenario: Member successfully leaved a specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "C" requested to leave the user's "A" organization
    Then user "C" should leave the relevant organization and receive the empty object


  #MPA-7229 (#9.1)
  Scenario: Origin owner successfully leaved own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to leave the user's "A" organization by its own decision
    Then user "A" should leave the own organization and receive the empty object


  #MPA-7229 (#9.2)
  Scenario: Fail to leave the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to leave some Organization "organizationId"
    Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#9.3)
  Scenario: Fail to leave the Organization upon the user hasn't became a member (wasn't invited) to any of the relevant Organizations
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "B" requested to leave the user's "A" organization
    Then the user "B" should receive error message: "user: 'null', name: 'userId "B"', is not a "member" of organization: 'user "A" organizationId'"


  #MPA-7229 (#9.4)
  Scenario: Fail to leave a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to leave the non-existent organization "organizationId"
    Then user "A" should receive the error message: "organizationId" doesn't exist



  #ADD ORG API KEY (token)

  #MPA-7229 (#10)
  Scenario: Successful adding of API key (token) for a specific Organization with relevant assigned roles (permission level for configuration service)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add the API keys "name" for user's "A" organization with assigned roles "owner", "admin" and "member"
    Then each of the API keys with assigned roles of "owner", "admin" and "member" should be added for the relevant organization


  #MPA-7229 (#10.1)
  Scenario: Successful adding either of accessible API keys (token) with the same "name" for a specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the API keys assigned by "owner", "admin" and "member" roles with specified "name" were added to user's "A" organization
    When the user "A" requested to add the API key with specified existent "name" for user's "A" organization assigned by either of roles "owner", "admin" or "member"
    Then the API key with the same (duplicated) "name" and relevant assigned "role" should be added for the relevant organization


  #MPA-7229 (#10.2)
  Scenario: Fail to add the API key (token) for a specific Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to add some API key for some Organization
    Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#10.3)
  Scenario: Fail to add the API key (token) for a specific Organization upon the user hadn't became the member in it with relevant role (permission level)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to add the API key "name" for user's "A" organization assigned by some "role"
    Then the user "A" should receive the error message: "user: 'null', name: 'userId "B"', is not a "member" of organization: 'user "A" organizationId'"


  #MPA-7229 (#10.3)
  Scenario: Fail to add the API key (token) for a specific Organization upon the relevant member doesn't have the owner's permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" either "admin" of the user's "A" organization
    When the user "B" requested to add the API key "name" for user's "A" organization assigned by some "role"
    Then user "B" should get an error message: "user: 'userId of user "B"', name: 'null', not in role Owner of organization: 'org "A" name'"


  #MPA-7229 (#10.4)
  Scenario: Fail to add the API key (token) for non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add the API key "name" for the non-existent organization "organizationId"
    #Then user "A" should receive the error message: "non-existent organizationId"


  #MPA-7229 (#10.5)
  Scenario: Fail to add the API key (token) for a specific Organization upon the role is else (invalid) apart of allowed
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add the API key "name" for user's "A" organization assigned by invalid "role" like "founder"
    Then the API key with assigned role "founder" shouldn't be added to the user's "A" organization "organizationId"
    #And the user "A" should receive the empty object



  #DELETE ORG API KEY (token)

  #MPA-7229 (#11)
  Scenario: Successful delete of API key (token) related to specific Organization with relevant assigned roles
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got the relevant API keys with assigned roles "owner", "admin" and "member"
    When the user "A" requested to delete the API key "name" in user's "A" organization with assigned role "owner"
    Then the API key with assigned roles of "owner" should be deleted
    And user "A" should receive successful response with the API keys of "admin" and "member" roles related to the relevant organization


  #MPA-7229 (#11.1)
  Scenario: Successful delete of multiple API key (token) from specific Organization upon these keys got the common name (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got the relevant API keys "name" with assigned roles "owner", "admin" and "member"
    And the user "A" requested to add the API key with specified existent "name" for user's "A" organization assigned by "admin" role
    When the user "A" requested to delete the API key with specified existent "name" for user's "A" organization assigned by "admin" role
    Then the both API keys with specified common (existent) "name" assigned by "admin" roles should be deleted
    And user "A" should receive successful response with the API keys of "owner" and "member" roles related to the relevant organization


  #MPA-7229 (#11.2)
  Scenario: Fail to add the API key (token) for a specific Organization upon the user hadn't became the member in it with relevant role (permission level)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to delete the API key "name" for user's "A" organization assigned by some "role"
    Then the user "A" should receive the error message: "user: 'null', name: 'userId "B"', is not a "member" of organization: 'user "A" organizationId'"


  #MPA-7229 (#11.4)
  Scenario: Fail to add the API key (token) for a specific Organization upon the relevant member doesn't have the owner's permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" either "admin" of the user's "A" organization
    When the user "B" requested to add the API key "name" for user's "A" organization assigned by some "role"
    Then user "B" should get an error message: "user: 'userId of user "B"', name: 'null', not in role Owner of organization: 'org "A" name'"


  #MPA-7229 (#11.5)
  Scenario: Fail to delete the API key (token) related to specific Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to delete some API key in some Organization
    #Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#11.6)
  Scenario: Fail to delete the API key (token) related to specific Organization upon the valid token doesn't have the owner's either admin's permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got the relevant API keys with assigned roles "owner", "admin" and "member"
    When the user "A" applied "token" of user "B" and requested to delete some API key "name" in user's "A" organization "organizationId"
    #Then the user "A" should receive the error message: "user: 'null', name: 'userId of user A', not in role Owner or Admin of organization: 'org "B" name'"


  #MPA-7229 (#11.7)
  Scenario: Fail to delete the API key (token) from non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to delete the API key "name" for the non-existent organization "organizationId"
    #Then user "A" should receive the error message: "non-existent organizationId"


  #MPA-7229 (#11.8)
  Scenario: Fail to delete non-existent (invalid) API key (token) from specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got the relevant API keys with assigned roles "owner", "admin" and "member"
    When the user "A" requested to delete the non-existent API key "name" in user's "A" organization
    Then any of the stored API keys shouldn't be deleted from the user's "A" organization "organizationId"
    #And user "A" should receive successful response with the API keys assigned by "owner", "admin" and "member" roles which related to the relevant organization



  #UPDATE MEMBER'S ROLE

  #MPA-7229 (#12)
  Scenario: Successful update of specific "member" role in a specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" requested to update the user "B" role to "admin" in user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "admin"


  #MPA-7229 (#12.1)
  Scenario: Successful remove (kick-out) the owner of a specific Organization
    Given the user "A" have got a valid "token" and "userId" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to remove the user "A" from user's "A" organization
    Then user "A" should abandon user's "A" organization and user "A" should get the empty object


  #MPA-7229 (#12.2) - CAN THE MEMBER WHO WAS GRANTED THE ADMIN/OWNER ROLE DOWNGRADE REAL OWNER TO MEMBER OR ADMIN OR KICKED-OUT REAL OWNER OR OWNER DOWNGRADE HIMSELF?
  Scenario: Successful remove (kick-out) of the "member" from specific Organization upon it's existent "member" was granted with admin role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user's "B" "member" role in the user's "A" organization was assigned to "admin" role by the owner - user "A"
    When the user "B" requested to remove user "C" from user's "A" organization "organizationId"
    Then user "C" should abandon user's "A" organization and user "B" should get the empty object


  #MPA-7229 (#12.3)
  Scenario: Successful remove (kick-out) of the "member" from specific Organization upon it's existent "member" was granted with owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by this user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority became the "member" of the user's "A" organization
    And the user's "B" "member" role in the user's "A" organization was assigned to "owner" role by the owner - user "A"
    When the user "B" requested to remove user "C" from user's "A" organization "organizationId"
    Then user "C" should abandon user's "A" organization and user "B" should get the empty object


  #MPA-7229 (#12.4) - FAIL OR SUCCESS ? CURRENTLY - SUCCESS
  Scenario: Successful remove (kick-out) of specific "member" from specific Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    And the user "A" was removed from user's "A" organization
    When the user "A" requested to remove the user "B" from user's "A" organization "organizationId"
    #Then the user "B" should abandon the user's "A" organization


  #MPA-7229 (#12.2)
  Scenario: Fail to remove the user from specific Organization if the token is invalid (expired)
    Given a user "A" have got the invalid either expired "token"
    When user "A" requested to remove some user "userId" from some organization "organizationId"
    Then user "A" should receive the error message: "Token verification failed"


  #MPA-7229 (#12.3)
  Scenario: Fail to remove the a specific "member" from a specific Organization upon the valid token doesn't have the owner's either admin's permission level
    Given each of the users "A" and "B" and "C" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" applied "token" of the user "B" and requested to remove the user "C" from the user's "A" organization
    And the user "C" applied own "token" and requested to remove the user "C" from the user's "A" organization
    Then user "A" should get an error message: "user: 'userId of user "A"', name: 'null', not in role Owner or Admin of organization: 'org "B" name'"
    And user "C" should get an error message: "user: 'userId of user "C"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#12.4)
  Scenario: Fail to remove the user from specific Organization upon the user is unauthorized (invalid or non-existent)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    And the user "D" have got the invalid "userId"
    When the user "A" requested to remove the user "D" from the user's "A" organization
    Then any "member" shouldn't be removed from the user's "A" organization and user "A" should get the empty object