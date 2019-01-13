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
  Scenario: Successful delete of multiple API keys (token) from specific Organization upon these keys got the common name (duplicate)
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got two relevant API keys with the common "name" with relevant assigned roles "owner" and "admin"
    When the user "A" requested to delete the API key with common existent "name" from own organization
    Then the both API keys with common existent "name" assigned with "owner" and "admin" roles should be deleted


  #MPA-7229 (#11.2) - SHOULD WE ALLOW IF THE ORIGIN OWNER WAS REMOVED ???
  Scenario: Successful delete some of accessible API keys (token) for relevant Organization upon the origin owner was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got relevant API key "name" with relevant assigned role "owner"
    And the user "A" was removed from user's "A" organization by its own decision
    When the user "A" requested to delete the API key "name" from own organization
    Then the API key with assigned "owner" role should be deleted from the relevant organization


  #MPA-7229 (#11.3)
  Scenario: Fail to delete the API key (token) related to specific Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to delete some API key in some Organization
    Then this user should receive the error message: "Token verification failed"


  #MPA-7229 (#11.4)
  Scenario: Fail to delete the API key (token) from related Organization upon the user hadn't became the member in it with relevant role (permission level)
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" applied "token" of user "B" and requested to delete the API key "name" from user's "A" organization
    Then user "A" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7229 (#11.6)
  Scenario: Fail to delete the API key (token) from related Organization upon the relevant member doesn't have the appropriate permission level
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And the organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got the relevant API key with assigned role "owner"
    And the user "B" have got the "userId" issued by relevant authority and became the "member" of the user's "A" organization
    When the user "B" requested to delete the API key "name" for user's "A" organization
    Then user "B" should get an error message: "user: 'userId "B"', name: 'null', not in role Owner of organization: 'org "A" name'"


  #MPA-7229 (#11.7)
  Scenario: Fail to delete the API key (token) from non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to delete the API key "name" for the non-existent organization "organizationId"
    Then user "A" should receive the error message: "organizationId" doesn't exist


  #MPA-7229 (#11.8)
  Scenario: Fail to delete non-existent (invalid) API key (token) from specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And user's "A" organization have got the relevant API keys with assigned roles "owner", "admin" and "member"
    When the user "A" requested to delete the non-existent API key "name" in user's "A" organization
    Then any of the stored API keys shouldn't be deleted from the user's "A" organization "organizationId"
    And user "A" should receive successful response with related API keys assigned by "owner", "admin" and "member" roles which related to the relevant organization
