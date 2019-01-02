package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class LeaveOrganizationTest extends Base {

  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration =
        StepVerifier.create(
                createService(testProfile5)
                    .leaveOrganization(new LeaveOrganizationRequest(token, organisationId)))
            .expectSubscription()
            .assertNext(
                x ->
                    StepVerifier.create(
                            service.getOrganizationMembers(
                                new GetOrganizationMembersRequest(organisationId, token)))
                        .expectSubscription()
                        .assertNext(
                            r ->
                                assertThat(
                                    Arrays.asList(r.members()),
                                    not(
                                        hasItem(
                                            new OrganizationMember(
                                                testProfile5.getUserId(),
                                                Role.Member.toString())))))
                        .verifyComplete())
            .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.leaveOrganization(new LeaveOrganizationRequest(token, "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullOrgIdShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.leaveOrganization(new LeaveOrganizationRequest(token, null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullTokenIdShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.leaveOrganization(new LeaveOrganizationRequest(null, organisationId)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithEmptyTokenIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.leaveOrganization(new LeaveOrganizationRequest(new Token(""), organisationId)),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullInnerTokenIdShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.leaveOrganization(
                new LeaveOrganizationRequest(new Token(null), organisationId)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.leaveOrganization(new LeaveOrganizationRequest(token, "orgNotExists")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration =
        assertMonoCompletesWithError(
            createService(invalidProfile)
                .leaveOrganization(new LeaveOrganizationRequest(token, "orgNotExists")),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }
}
