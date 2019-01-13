 #UPDATE MEMBER'S ROLE - SHOULD WE RETURN INFO ABOUT MEMBER WHOSE ROLE WAS UPDATED IN THE RELEVANT ORGANIZATION INSTEAD OF PLAIN ACKNOWLEDGEMENT???

  #MPA-7229 (#12)
  Scenario: Successful upgrade of specific member to admin role in the relevant Organization by the origin owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" requested to update the user "B" role "member" to "admin" in user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "admin"


  #MPA-7229 (#12.1)
  Scenario: Successful upgrade of specific member to owner role in the relevant Organization by the admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "B" requested to update the user "C" role "member" to "owner" in user's "A" organization
    Then user's "C" role in user's "A" organization should be updated to "admin"


  #MPA-7229 (#12.2)
  Scenario: Successful downgrade of "admin" role in the relevant Organization by the origin owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    When the user "A" requested to update the user "B" role "admin" to "member" in user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "member"


  #MPA-7229 (#12.3)
  Scenario: Successful downgrade of the granted "owner" role by the some member granted with "admin" role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "owner" of the user's "A" organization
    And the user "C" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    When the user "C" requested to update the user "B" role "owner" to "admin" in user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "admin"


  #MPA-7229 (#12.4) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful upgrade of specific admin to owner role in the relevant Organization by the origin owner who was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to update the user "B" role "admin" to "owner" in user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "owner"


  #MPA-7229 (#12.5) - SHOULD WE PROHIBIT ???
  Scenario: Successful downgrade of origin Organization owner to member role either by granted admin or owner role
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    When the user "B" requested to update the user "A" role origin "owner" to "member" in user's "A" organization
    Then user's "A" role in user's "A" organization should be updated to "member"


  #MPA-7229 (#12.6)
  Scenario: Fail to update some member role in some Organization if the token is invalid (expired)
    Given a user "A" have got the invalid either expired "token"
    When the user "A" requested to update the "some" user role "admin" to "owner" in some organization
    Then user "A" should receive the error message: "Token verification failed"


  #MPA-7229 (#12.6)
  Scenario: Fail to update some member role in the related Organization upon the user hadn't became the member in it with relevant role (permission level)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "A" applied "token" of user "B" and requested to update the user "C" role "member" to "admin" in user's "A" organization
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#12.7)
  Scenario: Fail to update some of the accessible member roles in the relevant Organization upon the member doesn't have the appropriate permission level
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    And the user "C" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "C" requested to update the user "B" role "admin" to "member" in user's "A" organization
    Then user "C" should get an error message: "user: 'userId "C"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#12.8)
  Scenario: Fail to update some of the member roles in the non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And there is no organization was created and stored
    When the user "A" requested to update the "some" user "role" in the non-existent organization "organizationId"
    Then user "A" should receive the error message: "organizationId" doesn't exist


  #MPA-7229 (#12.9)
  Scenario: Fail to delete non-existent (invalid) API key (token) from specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" which doesn't contain any "member" already created and owned by user "A"
    When the user "A" requested to update some "role" of the "non-existent" user in user's "A" organization
    Then any of the stored API keys shouldn't be deleted from the user's "A" organization "organizationId"
    And user "A" should get an error message: "user: 'userId "non-existent"', is not a member of organization: 'org "A" name'"