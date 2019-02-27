@Configuration-service-production-ready

Feature: Organization service members management - Update member role

  Organization Owner or Admin should be able to update the existing members roles (permission level) in relevant organization.
  Any of the members who posses Admin role couldn't upgrade themselves whereby only the members with higher - Owner roles are able to do that.
  Nevertheless any member with Admin or Owner role could downgrade themselves as do the members with Owner role could downgrade the members with Admin role.
  Nevertheless at leas one organization owner should be persisted.

  /**
  * #Note for the future implementation after the refactoring:
  * #Permission level (Owner/Admin/Member) for each API key is automatically updated to the relevant one upon the managers who already issued these API keys
  * #were upgraded or downgraded to one of the accessible roles (Owner/Admin/Member) in the relevant organization.
  */

  #__________________________________________________POSITIVE___________________________________________________________

  #MPA-7679 (#72)
  Scenario: Successful upgrade of specific "member" to "admin" role in the relevant Organization by the "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "A" requested to update the user "B" role to "admin" in the own organization
    Then user's "B" role in user's "A" organization should be updated to "admin"
    And user "A" should get successful response with the empty object


  #MPA-7679 (#73)
  Scenario: Successful upgrade of specific "member" to "owner" role in the relevant Organization by another "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "A" requested to update the user "B" role to "owner" in the own organization
    Then user's "B" role in user's "A" organization should be updated to "owner"
    And user "A" should get successful response with the empty object


  #MPA-7679 (#74)
  Scenario: Successful upgrade of "admin" to "owner" role in the relevant Organization by another "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    When the user "A" requested to update the user "B" role to "owner" in the own organization
    Then user's "B" role in user's "A" organization should be updated to "owner"
    And user "A" should get successful response with the empty object


  #MPA-7679 (#75)
  Scenario: Successful upgrade of specific "member" to "admin" role in the relevant Organization by the "admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "B" requested to update the user "C" role to "admin" in the user's "A" organization
    Then user's "C" role in user's "A" organization should be updated to "admin"
    And user "B" should get successful response with the empty object


  #MPA-7679 (#76)
  Scenario: Successful downgrade of the "owner" to "admin" role in the relevant Organization by another "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "owner" role
    When the user "A" requested to update the user "B" role to "admin" in the user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "admin"
    And user "A" should get successful response with the empty object


  #MPA-7679 (#77)
  Scenario: Successful downgrade of the "owner" to "member" role in the relevant Organization by another "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority were invited to user's "A" organization both with "owner" role
    When the user "B" requested to update the user "C" role to "member" in the user's "A" organization
    Then user's "C" role in user's "A" organization should be updated to "member"
    And user "B" should get successful response with the empty object


  #MPA-7679 (#78)
  Scenario: Successful downgrade of the "admin" to "member" role in the relevant Organization by the "owner"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "owner" role
    When the user "C" requested to update the user "B" role to "member" in the user's "A" organization
    Then user's "B" role in user's "A" organization should be updated to "member"
    And user "C" should get successful response with the empty object


  #MPA-7679 (#79)
  Scenario: Successful downgrade "admin" to "member" role in the relevant Organization by another "admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And each of the users "B" and "C" who have got the "userId" issued by relevant authority were invited to user's "A" organization both with "admin" role
    When the user "B" requested to update the user "C" role to "member" in the user's "A" organization
    Then user's "C" role in user's "A" organization should be updated to "member"
    And user "B" should get successful response with the empty object


  #MPA-7679 (#80)
  Scenario: Successful downgrade yourself as the "owner" to "member" either "admin" role in the relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "owner" role
    When the user "A" requested to update himself to role "member" either "admin" in the user's "A" organization
    Then user's "A" role in own organization should be updated to "member" either "admin"
    And user "A" should get successful response with the empty object


  #MPA-7679 (#81)
  Scenario: Successful downgrade yourself as "admin" to "member" role in the relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    When the user "B" requested to update himself to role "member" in the user's "A" organization
    Then user's "B" role in own organization should be updated to "member"
    And user "B" should get successful response with the empty object


  /**
    * #Note for the future implementation after the refactoring:
    *
    * ##MPA-7679 (#82)
    * #Scenario: Permission level of the "owner" API key (token) successfully downgraded to "admin" API key upon the Organization owner was downgraded to "admin"
    * #  Given the user "A" have got a valid "token" issued by relevant authority
    * #  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    * #  And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "owner", "admin" and "member"
    * #  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    * #  When the user "B" requested to update the user "A" role to "admin" in the user's "A" organization
    * #  Then the API key with assigned role "owner" should be updated to "admin" but the resting API keys "admin" and "member" should kept their roles
    * #  And the relevant secrets should be updated and stored in the Vault
    * #  And the API keys with assigned roles: "admin", "admin", "admin" and "member" should be persisted for the relevant organization


    * ##MPA-7679 (#83)
    * #Scenario: Permission level of the "owner" and "admin" API key (token) successfully downgraded to "member" API keys upon the Organization "owner" was downgraded to "member"
    * #  Given the user "A" have got a valid "token" issued by relevant authority
    * #  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    * #  And the user "A" requested to add each accessible API key "name" for own organization with assigned roles: "owner", "admin" and "member"
    * #  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    * #  When the user "B" requested to update the user "A" role to "member" in the user's "A" organization
    * #  Then all the API keys with managers' roles should be updated to "member" and the resting "member" API key should kept its role
    * #  And the relevant secrets should be updated and stored in the Vault
    * #  And the API keys with assigned roles: "member", "member" and "member" should be persisted for the relevant organization


    * ##MPA-7679 (#84)
    * #Scenario: Permission level of the "admin" API key (token) successfully downgraded to "member" API key upon the Organization "admin" was downgraded to "member"
    * #  Given the user "A" have got a valid "token" issued by relevant authority
    * #  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    * #  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    * #  And the user "B" requested to add the API key "name" for user's "A" organization with assigned role "admin"
    * #  When the user "A" requested to update the user "B" role to "member" in the own organization
    * #  Then the API key with assigned role "admin" should be updated to "member"
    * #  And the relevant secret should be updated and stored in the Vault
    * #  And only one API key with assigned role: "member" should be persisted for the relevant organization


    * ##MPA-7679 (#85)
    * #Scenario: Permission level of the "member" API key (token) successfully upgraded to "admin" API key upon the Organization "member" was promoted to "admin" again
    * #  Given the user "A" have got a valid "token" issued by relevant authority
    * #  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    * #  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "admin" role
    * #  And the user "B" requested to add the API keys "name" for user's "A" organization with assigned roles: "member" and "admin"
    * #  When the user "A" requested to update the user "B" role to "member" in the own organization
    * #  And the user "A" requested to update the user "B" role to "admin" in the own organization
    * #  Then the API key with assigned role "member" should be updated to "admin" and the resting "admin" API key should kept its role
    * #  And the relevant secret should be updated and stored in the Vault
    * #  And the API keys with assigned roles: "admin" and "admin" should be persisted for the relevant organization


    * ##MPA-7679 (#86)
    * #Scenario: Permission level of the "member" and "admin" API keys (token) successfully upgraded to "owner" API keys upon the Organization "admin" was promoted to "owner" again
    * #  Given the user "A" have got a valid "token" issued by relevant authority
    * #  And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    * #  And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with an "owner" role
    * #  And the user "B" requested to add each accessible API key "name" for user's "A" organization with assigned roles: "member", "admin" and "owner"
    * #  When the user "A" requested to update the user "B" role to "admin" in the own organization
    * #  And the user "A" requested to update the user "B" role to "owner" in the own organization
    * #  Then the API keys with assigned roles "member" and "admin" should be updated to "owner"
    * #  And the relevant secrets should be updated and stored in the Vault
    * #  And the API keys with assigned roles: "owner", "owner" and "owner" should be persisted for the relevant organization
    */


  #__________________________________________________NEGATIVE___________________________________________________________

  #MPA-7679 (#87)
  Scenario: Fail to downgrade yourself as the single "owner" to "member" either "admin" role in the relevant Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to update himself to role "member" either "admin" in the user's "A" organization
    Then user "A" should get an error message: "At least one Owner should be persisted in the organization: 'organizationId'"



  #MPA-7679 (#88)
  Scenario: Fail to upgrade a "member" either "admin" to "owner" role in the relevant Organization by the "admin"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    When the user "C" requested to update himself and the user "B" to role "owner" in the user's "A" organization
    Then user "C" for both requests should get an error message: ""user: 'userId "C"', name: 'null', role: 'Admin' cannot promote to a higher role: 'Owner'"



  #MPA-7679 (#89)
  Scenario: Fail to update any accessible member role in the relevant Organization by the "member"
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "admin" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    When the user "C" requested to update the user "B" to role "member" in the user's "A" organization
    Then user "C" should get an error message: ""user: 'userId "C"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#90)
  Scenario: Fail to update any accessible member role in the relevant Organization by the owner who was removed from own Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    And the user "B" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "owner" role
    And the user "C" who have got the "userId" issued by relevant authority was invited to user's "A" organization with a "member" role
    And the user "B" requested to remove the user "A" from user's "A" organization
    When the user "A" requested to update the user "C" to role "admin" in the user's "A" organization
    Then user "A" should get an error message: ""user: 'userId "A"', name: 'null', not in role Owner or Admin of organization: 'org "A" name'"


  #MPA-7679 (#91)
  Scenario: Fail to update some member role in some Organization if the token is invalid (expired)
    Given a user "D" have got the invalid either expired "token"
    When the user "D" requested to update the some user role in some organization
    Then user "D" should receive the error message: "Token verification failed"
