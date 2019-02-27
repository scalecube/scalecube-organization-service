package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class LeaveOrganizationTest extends Base {

  @Test
  void leaveOrganization() {
    addMemberToOrganization(organizationId, testProfile5);

    StepVerifier.create(
            createService(testProfile5)
                .leaveOrganization(new LeaveOrganizationRequest(token, organizationId)))
        .expectSubscription()
        .assertNext(
            x ->
                StepVerifier.create(
                        service.getOrganizationMembers(
                            new GetOrganizationMembersRequest(organizationId, token)))
                    .expectSubscription()
                    .assertNext(
                        response ->
                            assertThat(
                                Arrays.asList(response.members()),
                                not(
                                    hasItem(
                                        new OrganizationMember(
                                            testProfile5.getUserId(), Role.Member.toString())))))
                    .verifyComplete())
        .verifyComplete();
  }

  @Test
  void leaveOrganizationWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(token, "")),
        IllegalArgumentException.class);
  }

  @Test
  void leaveOrganizationWithNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(token, null)),
        NullPointerException.class);
  }

  @Test
  void leaveOrganizationWithNullTokenIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(null, organizationId)),
        NullPointerException.class);
  }

  @Test
  void leaveOrganizationWithEmptyTokenIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(new Token(""), organizationId)),
        IllegalArgumentException.class);
  }

  @Test
  void leaveOrganizationWithNullInnerTokenIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(new Token(null), organizationId)),
        NullPointerException.class);
  }

  @Test
  void leaveOrganizationOrgNotExistsShouldFailWithOrganizationNotFoundException() {
    assertMonoCompletesWithError(
        service.leaveOrganization(new LeaveOrganizationRequest(token, "orgNotExists")),
        OrganizationNotFoundException.class);
  }

  @Test
  void leaveOrganizationInvalidUserShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .leaveOrganization(new LeaveOrganizationRequest(token, "orgNotExists")),
        InvalidAuthenticationToken.class);
  }
}
