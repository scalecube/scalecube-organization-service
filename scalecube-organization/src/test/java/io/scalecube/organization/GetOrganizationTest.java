package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class GetOrganizationTest extends Base {

  /**
   * #MPA-7229 (#2) Scenario: Successful info get about specific Organization Given the user "A"
   * have got a valid "token" issued by relevant authority And only single organization
   * "organizationId" with specified "name" and "email" already created and owned by user "A" When
   * the user "A" requested to get the own organization info Then user "A" should receive the
   * successful response with relevant organization data
   */
  @Test
  public void getOrganization() {
    StepVerifier.create(service.getOrganization(new GetOrganizationRequest(token, organizationId)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(organizationId)))
        .expectComplete()
        .verify();
  }

  @Test
  public void getOrganizationNotMemberShouldFailWithAccessPermissionException() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .getOrganization(new GetOrganizationRequest(new Token("foo"), organizationId)),
        AccessPermissionException.class);
  }

  @Test
  public void getOrganizationShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.getOrganization(new GetOrganizationRequest(token, "bla")),
        EntityNotFoundException.class);
  }

  @Test
  public void getOrganizationShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .getOrganization(new GetOrganizationRequest(token, organizationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void getOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile).getOrganization(new GetOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void getOrganizationWithNullIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(testProfile).getOrganization(new GetOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  public void getOrganizationWithNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .getOrganization(new GetOrganizationRequest(null, organizationId)),
        NullPointerException.class);
  }

  @Test
  public void getOrganizationWithInvalidTokenShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .getOrganization(new GetOrganizationRequest(token, organizationId)),
        InvalidAuthenticationToken.class);
  }

  @Test
  public void getOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .getOrganization(new GetOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
  }

  @Test
  public void getOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile).getOrganization(new GetOrganizationRequest(new Token(""), "")),
        IllegalArgumentException.class);
  }
}
