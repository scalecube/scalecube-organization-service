package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class OrganizationServiceApiKeyTest extends Base {

  @Test
  public void addOrganizationApiKey() {
    final HashMap<String, String> claims = new HashMap<>();
    claims.put("role", "Owner");
    Duration duration =
        StepVerifier.create(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(token, organisationId, "apiKey", claims)))
            .expectSubscription()
            .assertNext(
                x -> {
                  Organization org = getOrganizationFromRepository(organisationId);
                  assertThat(org.apiKeys()[0].name(), is("apiKey"));
                })
            .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyNotOrgOwnerShouldFailWithAccessPermissionException() {
    Duration duration =
        expectError(
            createService(testProfile5)
                .addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        token, organisationId, "api_key", new HashMap<>())),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, "", "api_key", new HashMap<>())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyNullOrgIdShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, null, "api_key", new HashMap<>())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyNullApiKeyNameShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organisationId, null, new HashMap<>())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyEmptyApiKeyNameShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, organisationId, "", new HashMap<>())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    new Token(""), organisationId, "api_key", new HashMap<>())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(null, organisationId, "api_key", new HashMap<>())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    new Token(null), organisationId, "api_key", new HashMap<>())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyWithNullClaimsShouldPass() {
    Duration duration =
        StepVerifier.create(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(token, organisationId, "api_key", null)))
            .expectSubscription()
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration =
        expectError(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(token, "bla", "api_key", new HashMap<>())),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        token, organisationId, "api_key", new HashMap<>())),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKey() {
    Duration duration =
        StepVerifier.create(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        token,
                        organisationId,
                        "apiKey",
                        Arrays.asList("assertion")
                            .stream()
                            .collect(Collectors.toMap(x -> x, x -> x)))))
            .expectSubscription()
            .assertNext(
                x ->
                    StepVerifier.create(
                            service.deleteOrganizationApiKey(
                                new DeleteOrganizationApiKeyRequest(
                                    token, organisationId, "apiKey")))
                        .expectSubscription()
                        .assertNext(
                            k -> {
                              Organization org = getOrganizationFromRepository(organisationId);
                              assertThat(org.apiKeys(), emptyArray());
                            })
                        .verifyComplete())
            .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    Duration duration =
        expectError(
            createService(testProfile2)
                .addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(token, organisationId, "api_key", null)),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyUserNotAdminShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organisationId, service, testProfile2);

    Duration duration =
        expectError(
            createService(testProfile2)
                .addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(token, organisationId, "api_key", null)),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void addOrganizationApiKeyByAdmin() {
    Profile adminUser = testProfile2;
    addMemberToOrganization(organisationId, service, adminUser);

    // upgrade user to admin role
    consume(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organisationId, adminUser.getUserId(), Role.Admin.toString())));

    // add api key by admin
    Duration duration =
        StepVerifier.create(
                createService(adminUser)
                    .addOrganizationApiKey(
                        new AddOrganizationApiKeyRequest(token, organisationId, "apiKey", null)))
            .expectSubscription()
            .assertNext(
                x -> {
                  Organization org = getOrganizationFromRepository(organisationId);
                  assertThat(org.apiKeys()[0].name(), is("apiKey"));
                })
            .verifyComplete();
    assertNotNull(duration);
  }
}
