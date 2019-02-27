package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class DeleteOrganizationApiKeyTest extends Base {

  @Test
  void deleteOrganizationApiKey() {
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token,
                    organizationId,
                    "apiKey",
                    Stream.of("assertion").collect(Collectors.toMap(x -> x, x -> x)))))
        .expectSubscription()
        .assertNext(
            getOrganizationResponse -> {
              assertThat(getOrganizationResponse.apiKeys(), arrayWithSize(1));
              String apiKeyName = getOrganizationResponse.apiKeys()[0].name();
              StepVerifier.create(
                      service.deleteOrganizationApiKey(
                          new DeleteOrganizationApiKeyRequest(token, organizationId, apiKeyName)))
                  .expectSubscription()
                  .assertNext(
                      getOrganizationResponse2 ->
                          assertThat(getOrganizationResponse2.apiKeys(), emptyArray()))
                  .verifyComplete();
            })
        .verifyComplete();
  }

  @Test
  void deleteOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile2)
            .deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(token, organizationId, "api_key")),
        AccessPermissionException.class);
  }

  @Test
  void deleteOrganizationApiKeyEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(new DeleteOrganizationApiKeyRequest(token, "", "api_key")),
        IllegalArgumentException.class);
  }

  @Test
  void deleteOrganizationApiKeyOrgNotExistsShouldFailWithOrganizationNotFoundException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, "bla", "api_key")),
        OrganizationNotFoundException.class);
  }

  @Test
  void deleteOrganizationApiKeyNullOrgShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, null, "api_key")),
        NullPointerException.class);
  }

  @Test
  void deleteOrganizationApiKeyNullApiKeyNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, organizationId, null)),
        NullPointerException.class);
  }

  @Test
  void deleteOrganizationApiKeyEmptyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, organizationId, "")),
        IllegalArgumentException.class);
  }

  @Test
  void deleteOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(token, organizationId, "api_key")),
        InvalidAuthenticationToken.class);
  }

  @Test
  void deleteOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(null, organizationId, "api_key")),
        NullPointerException.class);
  }

  @Test
  void deleteOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(new Token(null), organizationId, "api_key")),
        NullPointerException.class);
  }

  @Test
  void deleteOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(new Token(""), organizationId, "api_key")),
        IllegalArgumentException.class);
  }
}
