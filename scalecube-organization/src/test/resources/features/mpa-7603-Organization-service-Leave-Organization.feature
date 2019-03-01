@Configuration-service-production-ready

Feature: Organization service Org management - Leave Organization

  Any member of the relevant Organization should be able to leave it by own decision.
  Nevertheless at least one Owner should be persisted in the relevant Organization.


  #__________________________________________________POSITIVE___________________________________________________________
  

  #MPA-7603 (#28)
  Scenario: Owner successfully leaved a relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    When the user "A" requested to leave own organization
    Then user "A" should leave the relevant organization and receive the empty object


  #MPA-7603 (#29)
  Scenario: Admin successfully leaved a relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    When the user "B" requested to leave user's "A" organization
    Then user "B" should leave the relevant organization and receive the empty object


  #MPA-7603 (#30)
  Scenario: Member successfully leaved a relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "C" requested to leave user's "A" organization
    Then user "C" should leave the relevant organization and receive the empty object


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-7603 (#31)
  Scenario: Fail to leave a relevant Organization by the single owner
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to leave own organization
    Then user "A" should should receive the error message:"At least one Owner should be persisted in the organization: 'org A id'"


  #MPA-7603 (#32) - should we return an error like " is not a "member" of organization" instead of empty object?
  Scenario: Fail to leave the Organization upon the user wasn't invited to any of the relevant Organizations
    Given each of the users "A" and "B" have got personal valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "B" requested to leave the user's "A" organization
    Then the user "B" should receive successful response with empty object


  #MPA-7603 (#33)
  Scenario: Fail to leave a non-existent Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And there is no organization "organizationId" was created and stored
    When the user "A" requested to leave the non-existent organization "organizationId"
    Then user "A" should receive the error message with non-existent: "organizationId"


  #MPA-7603 (#34)
  Scenario: Fail to leave the Organization if the token is invalid (expired)
    Given a user have got the invalid either expired "token"
    When this user requested to leave some Organization "organizationId"
    Then this user should receive the error message: "Token verification failed"