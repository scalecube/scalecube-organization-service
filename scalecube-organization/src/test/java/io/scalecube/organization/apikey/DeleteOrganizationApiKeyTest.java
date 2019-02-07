package io.scalecube.organization.apikey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.Base;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@Disabled
public class DeleteOrganizationApiKeyTest extends Base {

  /**
   *   #MPA-7229 (#11)
   * Scenario: Successful delete of API key (token) related to specific Organization with relevant assigned roles
   *   Given the user "A" have got a valid "token" issued by relevant authority
   *   And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
   *   And user's "A" organization have got the relevant API keys with assigned roles "owner", "admin" and "member"
   *   When the user "A" requested to delete the API key "name" in user's "A" organization with assigned role "owner"
   *   Then the API key with assigned roles of "owner" should be deleted
   *   And user "A" should receive successful response with the API keys of "admin" and "member" roles related to the relevant organization
   */
  @Test
  public void deleteOrganizationApiKey() {
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token,
                    organizationId,
                    "apiKey",
                    Arrays.asList("assertion").stream().collect(Collectors.toMap(x -> x, x -> x)))))
        .expectSubscription()
        .assertNext(
            getOrganizationResponse ->
                StepVerifier.create(
                        service.deleteOrganizationApiKey(
                            new DeleteOrganizationApiKeyRequest(
                                token, organizationId, getOrganizationResponse.apiKeys()[0].key())))
                    .expectSubscription()
                    .assertNext(
                        getOrganizationResponse2 ->
                            assertThat(getOrganizationResponse2.apiKeys(), emptyArray()))
                    .verifyComplete())
        .verifyComplete();
  }

  @Test
  public void deleteOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile2)
            .deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(token, organizationId, "api_key")),
        AccessPermissionException.class);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(new DeleteOrganizationApiKeyRequest(token, "", "api_key")),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganizationApiKeyOrgNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, "bla", "api_key")),
        EntityNotFoundException.class);
  }

  @Test
  public void deleteOrganizationApiKeyNullOrgShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, null, "api_key")),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyNullApiKeyNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, organizationId, null)),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, organizationId, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(token, organizationId, "api_key")),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void deleteOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(null, organizationId, "api_key")),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(new Token(null), organizationId, "api_key")),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(new Token(""), organizationId, "api_key")),
        IllegalArgumentException.class);
  }
}
