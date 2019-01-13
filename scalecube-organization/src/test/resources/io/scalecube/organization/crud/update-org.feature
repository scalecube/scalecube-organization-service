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
