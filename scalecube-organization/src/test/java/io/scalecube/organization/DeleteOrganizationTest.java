package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class DeleteOrganizationTest extends Base {

  @Test
  public void deleteOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile2)
            .deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(token, organisationId, "api_key")),
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
            new DeleteOrganizationApiKeyRequest(token, organisationId, null)),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyNameShouldFailWithIllegalArgumentException() {

    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(token, organisationId, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {

    assertMonoCompletesWithError(
        createService(invalidProfile)
            .deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(token, organisationId, "api_key")),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void deleteOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(null, organisationId, "api_key")),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(new Token(null), organisationId, "api_key")),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {

    assertMonoCompletesWithError(
        service.deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(new Token(""), organisationId, "api_key")),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganizationInvalidTokenShouldFailWithInvalidAuthenticationToken() {

    assertMonoCompletesWithError(
        createService(invalidProfile)
            .deleteOrganization(new DeleteOrganizationRequest(token, organisationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void deleteOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
  }

  @Test
  public void deleteOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {

    assertMonoCompletesWithError(
        createService(testProfile).deleteOrganization(new DeleteOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganizationWithNullIdShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        createService(testProfile).deleteOrganization(new DeleteOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationWithNullTokenShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(null, organisationId)),
        NullPointerException.class);
  }

  @Test
  public void deleteOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(new Token(""), organisationId)),
        IllegalArgumentException.class);
  }

  @Test
  public void deleteOrganization() {
    String id = createRandomOrganization();

    StepVerifier.create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.deleted(), is(true)))
        .expectComplete()
        .verify();
    deleteOrganization(id);
  }
}
