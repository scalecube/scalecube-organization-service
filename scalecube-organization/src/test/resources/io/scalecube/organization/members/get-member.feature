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
