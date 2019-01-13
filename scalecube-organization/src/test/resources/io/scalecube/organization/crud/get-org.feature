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
