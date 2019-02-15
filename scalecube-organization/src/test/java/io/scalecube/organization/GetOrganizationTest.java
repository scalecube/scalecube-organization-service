package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class GetOrganizationTest extends Base {

  @Test
  void getOrganization() {
    StepVerifier.create(service.getOrganization(new GetOrganizationRequest(token, organizationId)))
        .expectSubscription()
        .assertNext(response -> assertEquals(response.id(), organizationId))
        .expectComplete()
        .verify();
  }

  @Test
  void getOrganizationNotMemberShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .getOrganization(new GetOrganizationRequest(new Token("foo"), organizationId)),
        AccessPermissionException.class);
  }

  @Test
  void getOrganizationShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.getOrganization(new GetOrganizationRequest(token, "bla")),
        EntityNotFoundException.class);
  }

  @Test
  void getOrganizationShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .getOrganization(new GetOrganizationRequest(token, organizationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  void getOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile).getOrganization(new GetOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  void getOrganizationWithNullIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(testProfile).getOrganization(new GetOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  void getOrganizationWithNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .getOrganization(new GetOrganizationRequest(null, organizationId)),
        NullPointerException.class);
  }

  @Test
  void getOrganizationWithInvalidTokenShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .getOrganization(new GetOrganizationRequest(token, organizationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  void getOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .getOrganization(new GetOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
  }

  @Test
  void getOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile).getOrganization(new GetOrganizationRequest(new Token(""), "")),
        IllegalArgumentException.class);
  }
}
