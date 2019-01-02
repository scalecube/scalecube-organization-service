package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class LeaveOrganizationTest extends Base {

  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile5);

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
                                            testProfile5.getUserId(), Role.Member.toString())))))
                    .verifyComplete())
        .verifyComplete();
  }

  @Test
  public void leaveOrganizationWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  public void leaveOrganizationWithNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  public void leaveOrganizationWithNullTokenIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(null, organisationId)),
        NullPointerException.class);
  }

  @Test
  public void leaveOrganizationWithEmptyTokenIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(new Token(""), organisationId)),
        IllegalArgumentException.class);
  }

  @Test
  public void leaveOrganizationWithNullInnerTokenIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(new Token(null), organisationId)),
        NullPointerException.class);
  }

  @Test
  public void leaveOrganizationOrgNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(token, "orgNotExists")),
        EntityNotFoundException.class);
  }

  @Test
  public void leaveOrganizationInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .leaveOrganization(new LeaveOrganizationRequest(token, "orgNotExists")),
        InvalidAuthenticationToken.class);
  }
}
