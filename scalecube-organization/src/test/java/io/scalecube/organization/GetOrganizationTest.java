package io.scalecube.organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
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
  public void getOrganization_not_a_member_should_fail_with_AccessPermissionException() {
    Duration duration =
        expectError(
            createService(testProfile5)
                .getOrganization(
                    new GetOrganizationRequest(new Token("foo"), organisationId)),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_should_fail_with_EntityNotFoundException() {
    Duration duration =
        expectError(
            service.getOrganization(new GetOrganizationRequest(token, "bla")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_should_fail_with_InvalidAuthenticationToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .getOrganization(new GetOrganizationRequest(token, organisationId)),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_empty_id_should_fail_with_IllegalArgumentException() {
    Duration duration =
        expectError(
            createService(testProfile).getOrganization(new GetOrganizationRequest(token, "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_null_id_should_fail_with_NullPointerException() {
    Duration duration =
        expectError(
            createService(testProfile).getOrganization(new GetOrganizationRequest(token, null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_null_token_should_fail_with_NullPointerException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .getOrganization(new GetOrganizationRequest(null, organisationId)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_invalid_token_should_fail_with_InvalidAuthenticationToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .getOrganization(new GetOrganizationRequest(token, organisationId)),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_id_not_exists_should_fail_with_EntityNotFoundException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .getOrganization(new GetOrganizationRequest(token, "orgIdNotExists")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganization_with_empty_token_should_fail_with_IllegalArgumentException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .getOrganization(new GetOrganizationRequest(new Token(""), "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }
}
