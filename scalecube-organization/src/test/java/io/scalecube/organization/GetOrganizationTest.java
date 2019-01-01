package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class GetOrganizationTest extends Base {

  
  /**
  #MPA-7229 (#2)
  Scenario: Successful info get about specific Organization
    Given the user "A" have got a valid "token" issued by relevant authority
    And only single organization "organizationId" with specified "name" and "email" already created and owned by user "A"
    When the user "A" requested to get the own organization info
    Then user "A" should receive the successful response with relevant organization data
   */
  @Test
  public void getOrganization() {
    Duration duration =
        StepVerifier.create(
                service.getOrganization(new GetOrganizationRequest(token, organisationId)))
            .expectSubscription()
            .assertNext((r) -> assertThat(r.id(), is(organisationId)))
            .expectComplete()
            .verify();
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationNotMemberShouldFailWithAccessPermissionException() {
    Duration duration =
        expectError(
            createService(testProfile5)
                .getOrganization(
                    new GetOrganizationRequest(new Token("foo"), organisationId)),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationShouldFailWithEntityNotFoundException() {
    Duration duration =
        expectError(
            service.getOrganization(new GetOrganizationRequest(token, "bla")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationShouldFailWithInvalidAuthenticationToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .getOrganization(new GetOrganizationRequest(token, organisationId)),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            createService(testProfile).getOrganization(new GetOrganizationRequest(token, "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationWithNullIdShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            createService(testProfile).getOrganization(new GetOrganizationRequest(token, null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationWithNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .getOrganization(new GetOrganizationRequest(null, organisationId)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationWithInvalidTokenShouldFailWithInvalidAuthenticationToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .getOrganization(new GetOrganizationRequest(token, organisationId)),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .getOrganization(new GetOrganizationRequest(token, "orgIdNotExists")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .getOrganization(new GetOrganizationRequest(new Token(""), "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }
}
