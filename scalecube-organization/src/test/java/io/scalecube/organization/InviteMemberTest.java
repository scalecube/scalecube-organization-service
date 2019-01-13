package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class InviteMemberTest extends Base {

  @Test
  public void inviteMember() {
    addMemberToOrganization(organizationId, testProfile2);
    StepVerifier.create(
            service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationId, token)))
        .expectSubscription()
        .assertNext(
            (r) -> {
              Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());

              assertThat(
                  members.get().map(OrganizationMember::id).collect(Collectors.toList()),
                  hasItem(testProfile2.getUserId()));
              assertThat(
                  members
                      .get()
                      .filter(i -> Objects.equals(i.id(), testProfile2.getUserId()))
                      .findFirst()
                      .orElseThrow(IllegalStateException::new),
                  is(notNullValue()));
            })
        .verifyComplete();
  }

  @Test
  public void inviteMemberNullRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, testProfile5.getUserId(), null)),
        IllegalArgumentException.class);
  }

  @Test
  public void inviteMemberEmptyRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, testProfile5.getUserId(), "")),
        IllegalArgumentException.class);
  }

  @Test
  public void inviteMemberInvalidRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, testProfile5.getUserId(), "bla")),
        IllegalArgumentException.class);
  }

  @Test
  public void inviteMemberHigherRoleShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organizationId, testProfile2, Role.Admin);
    assertMonoCompletesWithError(
        createService(testProfile2)
            .inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Owner.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void inviteMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, "", testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
  }

  @Test
  public void inviteMemberNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, null, testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  public void inviteMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(token, organizationId, "", Role.Member.toString())),
        IllegalArgumentException.class);
  }

  @Test
  public void inviteMemberNullUserIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, organizationId, null, Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  public void inviteMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                new Token(""), organizationId, testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
  }

  @Test
  public void inviteMemberNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                null, organizationId, testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  public void inviteMemberNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                new Token(null), organizationId, testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
  }

  @Test
  public void inviteMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.inviteMember(
            new InviteOrganizationMemberRequest(
                token, "orgNotExists", testProfile5.getUserId(), Role.Member.toString())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMemberShouldFailWithInvalidAuthenticationToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Member.toString())),
        InvalidAuthenticationToken.class);
  }
}
