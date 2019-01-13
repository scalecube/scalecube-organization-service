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
