  #ADD ORG API KEY (token)

  #MPA-7229 (#10)
  Scenario: Successful adding of API key (token) for a specific Organization with relevant assigned roles (permission level for configuration service)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add each of the API keys "name" for own organization with relevant assigned roles "owner", "admin" and "member"
    Then each of the API keys with assigned roles of "owner", "admin" and "member" should be added for the relevant organization


  #MPA-7229 (#10.1)
  Scenario: Successful adding any of accessible API keys (token) with the same "name" (duplicate) for a specific Organization by Admin
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "admin" of the user's "A" organization
    And each of the API keys assigned by "owner", "admin" and "member" roles with specified "name" were added to user's "A" organization by it's owner
    When the user "B" requested to add the API keys with specified existent "name" for user's "A" organization assigned with "owner", "admin" and "member" roles
    Then each of the relevant API keys with the same (duplicated) "name" and relevant assigned role should be added for the relevant organization


  #MPA-7229 (#10.2)
  Scenario: Successful adding some of accessible API keys (token) for relevant Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    Given the user "B" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "A" was downgraded to Member level by user "B"
    When the user "A" requested to add the API key "name" for own organization with relevant assigned role "owner"
    Then the API key with assigned "owner" role should not be added for the relevant organization
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"

  #MPA-7229 (#10.3)
  Scenario: Fail to add the API key (token) for a specific Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to add some API key for some Organization
    Then this user should receive the error message: "Token verification failed"

   refactored:
  #MPA-7229 (#10.3)
  Scenario: Fail to add an API key if the token is expired
    Given a user have got an expired "token"
    When this user requested to add some API key for some Organization
    Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#10.4)
  Scenario: Fail to add the API key (token) for a specific Organization upon the user hadn't became the member in it with relevant role (permission level)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to add the API key "name" for user's "A" organization with some assigned "role"
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#10.6)
  Scenario: Fail to add the API key (token) for a specific Organization upon the relevant member doesn't have appropriate permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "B" requested to add the API key "name" for user's "A" organization assigned by some "role"
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#10.7)
  Scenario: Fail to add the API key (token) for non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add the API key "name" with some "role" for the non-existent organization "organizationId"
    Then user "A" should receive the error message: "organizationId" doesn't exist


  #MPA-7229 (#10.8) - SHOULD WE RETURN THE RELEVANT ERROR MESSAGE - UNAVAILABLE ROLE IS APPLIED??? INSTEAD OF SET IT TO THE MEMBER ROLE AS DEFAULT
  Scenario: Fail to add the API key (token) for a specific Organization upon the requested role differs (invalid) from allowed
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to add the API key "name" for user's "A" organization assigned by invalid "role" like "boss"
    Then the API key with assigned role "member" by default should be added to the user's "A" organization "organizationId"


  #MPA-7229 (#10.9) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful adding some of accessible API keys (token) for relevant Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    Given the user "B" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user "B" is an "Owner" in organization "organizationId"
    When the user "A" requested to add the API key "name" for own organization with relevant assigned role "owner"
    And the user "A" was removed from user's "A" organization by user "B"
    Then the API key with assigned "owner" role should not be removed