package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class DeleteOrganizationTest extends Base {

  @Test
  void deleteOrganizationInvalidTokenShouldFailWithInvalidAuthenticationToken() {

    assertMonoCompletesWithError(
        createService(invalidProfile)
            .deleteOrganization(new DeleteOrganizationRequest(token, organizationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  void deleteOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
  }

  @Test
  void deleteOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {

    assertMonoCompletesWithError(
        createService(testProfile).deleteOrganization(new DeleteOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  void deleteOrganizationWithNullIdShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        createService(testProfile).deleteOrganization(new DeleteOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  void deleteOrganizationWithNullTokenShouldFailWithNullPointerException() {

    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(null, organizationId)),
        NullPointerException.class);
  }

  @Test
  void deleteOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .deleteOrganization(new DeleteOrganizationRequest(new Token(""), organizationId)),
        IllegalArgumentException.class);
  }

  @Test
  void deleteOrganization() {
    String id = createRandomOrganization();
    StepVerifier.create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .assertNext(response -> assertTrue(response.deleted()))
        .expectComplete()
        .verify();
  }
}
