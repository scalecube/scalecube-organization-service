package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.security.api.Profile;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

class OrganizationServiceApiKeyTest extends Base {

  private String apiKeyName = "apiKey" + System.currentTimeMillis();

  static Role[] roles() {
    return Role.values();
  }

  @ParameterizedTest()
  @MethodSource("roles")
  void addOrganizationApiKey(Role role) {
    final HashMap<String, String> claims = new HashMap<>();
    claims.put("role", role.name());
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, apiKeyName, claims)))
        .expectSubscription()
        .assertNext(
            x -> {
              Organization org = getOrganizationFromRepository(organizationId);
              assertThat(org.apiKeys().iterator().next().name(), equalTo(apiKeyName));
            })
        .verifyComplete();
  }

  @Test
  void failToAddOrganizationApiKeyWithTheSameName() {
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
        .expectErrorMessage(String.format("apiKey name:'%s' already exists", apiKeyName))
        .verify();
  }

  @Test
  @Disabled // todo
  void failToAddOrganizationApiKeyByNonOwner() {
    Organization organisation2 = createOrganization(randomString());
    String organizationId2 = organisation2.id();

    addMemberToOrganization(organizationId2, testProfile2, Role.Owner);
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId2, testProfile.userId(), Role.Member.toString())))
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

  @Test
  void addOrganizationApiKeyNotOrgOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token, organizationId, "api_key", new HashMap<>())),
        AccessPermissionException.class);
  }

  @Test
  void addOrganizationApiKeyEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, "", "api_key", new HashMap<>())),
        IllegalArgumentException.class);
  }

  @Test
  void addOrganizationApiKeyNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, null, "api_key", new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  void addOrganizationApiKeyNullApiKeyNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organizationId, null, new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  void addOrganizationApiKeyEmptyApiKeyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organizationId, "", new HashMap<>())),
        IllegalArgumentException.class);
  }

  @Test
  void addOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                new Token(""), organizationId, "api_key", new HashMap<>())),
        IllegalArgumentException.class);
  }

  @Test
  void addOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(null, organizationId, "api_key", new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  void addOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                new Token(null), organizationId, "api_key", new HashMap<>())),
        NullPointerException.class);
  }

  @Test
  void addOrganizationApiKeyWithNullClaimsShouldPass() {
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, "api_key", null)))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  @Test
  void addOrganizationApiKeyOrgNotExistsShouldFailWithOrganizationNotFoundException() {
    assertMonoCompletesWithError(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, "bla", "api_key", new HashMap<>())),
        OrganizationNotFoundException.class);
  }

  @Test
  void addOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token, organizationId, "api_key", new HashMap<>())),
        InvalidAuthenticationToken.class);
  }

  @Test
  void addOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile2)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, "api_key", null)),
        AccessPermissionException.class);
  }

  @Test
  void addOrganizationApiKeyUserNotAdminShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organizationId, testProfile2);
    assertMonoCompletesWithError(
        createService(testProfile2)
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organizationId, "api_key", null)),
        AccessPermissionException.class);
  }

  @Test
  void addOrganizationApiKeyByAdmin() {
    Profile adminUser = testProfile2;
    addMemberToOrganization(organizationId, adminUser);

    // upgrade user to admin role
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, adminUser.userId(), Role.Admin.toString())))
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
              assertThat(org.apiKeys().iterator().next().name(), equalTo("apiKey"));
            })
        .verifyComplete();
  }
}
