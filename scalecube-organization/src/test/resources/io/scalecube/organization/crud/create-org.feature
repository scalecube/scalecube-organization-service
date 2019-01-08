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

