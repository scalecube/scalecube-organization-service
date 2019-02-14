package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InviteMemberTest extends Base {

  @Test
  void inviteMember() {
    addMemberToOrganization(organizationId, testProfile2);
    StepVerifier.create(
            service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationId, token)))
        .expectSubscription()
        .assertNext(
            response ->
                assertTrue(
                    Arrays.stream(response.members())
                        .anyMatch(member -> testProfile2.getUserId().equals(member.id()))))
        .verifyComplete();
  }

  @Test
  void inviteMemberNullRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, testProfile5.getUserId(), null)),
        IllegalArgumentException.class);
  }

  @Test
  void inviteMemberEmptyRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, testProfile5.getUserId(), "")),
        IllegalArgumentException.class);
  }

  @Test
  void inviteMemberInvalidRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, testProfile5.getUserId(), "bla")),
        IllegalArgumentException.class);
  }

  @Test
  void inviteMemberHigherRoleShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organizationId, testProfile2, Role.Admin);
    assertMonoCompletesWithError(
        createService(testProfile2)
            .inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Owner.toString())),
        AccessPermissionException.class);
  }

  @Test
  void inviteMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, "", testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
  }

  @Test
  void inviteMemberNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, null, testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  void inviteMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(token, organizationId, "", Role.Member.toString())),
        IllegalArgumentException.class);
  }

  @Test
  void inviteMemberNullUserIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, null, Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  void inviteMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                new Token(""), organizationId, testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
  }

  @Test
  void inviteMemberNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                null, organizationId, testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  void inviteMemberNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                new Token(null), organizationId, testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  void inviteMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, "orgNotExists", testProfile5.getUserId(), Role.Member.toString())),
        EntityNotFoundException.class);
  }

  @Test
  void inviteMemberShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Member.toString())),
        InvalidAuthenticationToken.class);
  }
}
