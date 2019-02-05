package io.scalecube.organization.apikey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.organization.Base;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@Disabled
public class OrganizationServiceApiKeyTest extends Base {
  public static Role[] roles() {
    return Role.values();
  }

  String apiKeyName = "apiKey" + System.currentTimeMillis();

  /**
   * #MPA-7229 (#10)</br> Scenario: Successful adding of API key (token) for a specific Organization
   * with relevant assigned roles (permission level for configuration service)</br> Given the user
   * "A" have got a valid "token" issued by relevant authority</br> And only single organization
   * "organizationId" with specified "name" and "email" already created and owned by user "A"</br>
   * When the user "A" requested to add each of the API keys "name" for own organization with
   * relevant assigned roles "owner", "admin" and "member"</br> Then each of the API keys with
   * assigned roles of "owner", "admin" and "member" should be added for the relevant organization
   */
  @ParameterizedTest()
  @MethodSource("roles")
  public void addOrganizationApiKey(Role role) {
    final HashMap<String, String> claims = new HashMap<>();
    claims.put("role", role.name());
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, apiKeyName, claims)))
        .expectSubscription()
        .assertNext(
            x -> {
              Organization org = getOrganizationFromRepository(organizationId);
              assertThat(org.apiKeys()[0].name(), equalTo(apiKeyName));
            })
        .verifyComplete();
  }

  /**
   * #MPA-7229 (#10.1).</br> Scenario: Successful adding any of accessible API keys (token) with the
   * same "name" (duplicate) for a specific Organization by Admin</br> Given the user "A" have got a
   * valid "token" issued by relevant authority</br> And only single organization "organizationId"
   * with specified "name" and "email" already created and owned by user "A"</br> And the user "B"
   * have got the "userId" issued by relevant authority and became the "admin" of the user's "A"
   * organization</br> And each of the API keys assigned by "owner", "admin" and "member" roles with
   * specified "name" were added to user's "A" organization by it's owner</br> When the user "B"
   * requested to add the API keys with specified existent "name" for user's "A" organization
   * assigned with "owner", "admin" and "member" roles<br>
   * Then each of the relevant API keys with the same (duplicated) "name" and relevant assigned role
   * should not be added for the relevant organization</br>
   */
  @Test
  public void failToAddOrganizationApiKeyWithTheSameName() {
    final HashMap<String, String> claims = new HashMap<>();
    claims.put("role", "Owner");
    StepVerifier.create(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organizationId, apiKeyName, claims)))
    .assertNext(Assertions::assertNotNull)
    .verifyComplete();
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, apiKeyName, claims)))
    
        .expectSubscription()
        .expectErrorMessage("apiKeyName already exists");
  }

  /**
   * #MPA-7229 (#10.2).</br> Scenario: Successful adding some of accessible API keys (token) for
   * relevant Organization upon the origin owner was removed from own Organization</br> Given the
   * user "A" have got a valid "token" issued by relevant authority</br> Given the user "B" have got
   * a valid "token" issued by relevant authority</br> And only single organization "organizationId"
   * with specified "name" and "email" already created and owned by user "A"</br> And the user "A"
   * was downgraded to Member level by user "B"</br> When the user "A" requested to add the API key
   * "name" for own organization with relevant assigned role "owner"</br> Then the API key with
   * assigned "owner" role should not be added for the relevant organization</br> Then user "A"
   * should get an error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of
   * organization: 'org "A" name'"</br>
   */
  @Test
  public void failToAddOrganizationApiKeyByNonOwner() {
    Organization organisation2 = createOrganization(randomString());
    String organizationId2 = organisation2.id();
    
    addMemberToOrganization(organizationId2, testProfile2, Role.Owner);
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId2, testProfile.getUserId(), Role.Member.toString())))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    final HashMap<String, String> claims = new HashMap<>();
    claims.put("role", "Owner");
    String expectedErrorMessage = "not in role Owner or Admin of organization";

    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, apiKeyName, claims)))
        .expectSubscription()
        .expectErrorMatches(ex -> ex.getMessage().contains(expectedErrorMessage))
        .verify();
    StepVerifier.create(
        service.deleteOrganization(new DeleteOrganizationRequest(token, organizationId2)))
    .assertNext(result -> assertTrue(result.deleted(), "failed to delete organization"))
    .verifyComplete();
  }
  /**
   * #MPA-7229 (#10.6)</br> Scenario: Fail to add the API key (token) for a specific Organization
   * upon the relevant member doesn't have appropriate permission level</br> Given each of the users
   * "A" and "B" have got personal valid "token" issued by relevant authority</br> And the
   * organization "organizationId" with specified "name" and "email" already created and owned by
   * user "A"</br> And the user "B" have got the "userId" issued by relevant authority and became
   * the "member" of the user's "A" organization</br> When the user "B" requested to add the API key
   * "name" for user's "A" organization assigned by some "role"</br> Then user "B" should get an
   * error message: "user: 'userId "B"', name: 'null', not in role Owner or Admin of organization:
   * 'org "A" name'"
   */
  @Test
  public void addOrganizationApiKeyNotOrgOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token, organizationId, "api_key", new HashMap<>())),
        AccessPermissionException.class);
  }

  @Test
  public void addOrganizationApiKeyEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, "", "api_key", new HashMap<>())),
        IllegalArgumentException.class);
  }

  @Test
  public void addOrganizationApiKeyNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, null, "api_key", new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  public void addOrganizationApiKeyNullApiKeyNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organizationId, null, new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  public void addOrganizationApiKeyEmptyApiKeyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organizationId, "", new HashMap<>())),
        IllegalArgumentException.class);
  }

  @Test
  public void addOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                new Token(""), organizationId, "api_key", new HashMap<>())),
        IllegalArgumentException.class);
  }

  @Test
  public void addOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(null, organizationId, "api_key", new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  public void addOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                new Token(null), organizationId, "api_key", new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  public void addOrganizationApiKeyWithNullClaimsShouldPass() {
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, "api_key", null)))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  @Test
  public void addOrganizationApiKeyOrgNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, "bla", "api_key", new HashMap<>())),
        EntityNotFoundException.class);
  }

  @Test
  public void addOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token, organizationId, "api_key", new HashMap<>())),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void addOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile2)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, "api_key", null)),
        AccessPermissionException.class);
  }

  @Test
  public void addOrganizationApiKeyUserNotAdminShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organizationId, testProfile2);
    assertMonoCompletesWithError(
        createService(testProfile2)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, "api_key", null)),
        AccessPermissionException.class);
  }

  @Test
  public void addOrganizationApiKeyByAdmin() {
    Profile adminUser = testProfile2;
    addMemberToOrganization(organizationId, adminUser);

    // upgrade user to admin role
    StepVerifier.create(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organizationId, adminUser.getUserId(), Role.Admin.toString())))
    .assertNext(Assertions::assertNotNull)
    .verifyComplete();
    // add api key by admin
    StepVerifier.create(
            createService(adminUser)
                .addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(token, organizationId, "apiKey", null)))
        .expectSubscription()
        .assertNext(
            x -> {
              Organization org = getOrganizationFromRepository(organizationId);
              assertThat(org.apiKeys()[0].name(), equalTo("apiKey"));
            })
        .verifyComplete();
  }
}
