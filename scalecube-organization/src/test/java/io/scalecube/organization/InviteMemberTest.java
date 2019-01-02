package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
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
    addMemberToOrganization(organisationId, service, testProfile2);
    Duration duration =
        StepVerifier.create(
                service.getOrganizationMembers(
                    new GetOrganizationMembersRequest(organisationId, token)))
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
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullRoleShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organisationId, testProfile5.getUserId(), null)),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyRoleShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organisationId, testProfile5.getUserId(), "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberInvalidRoleShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organisationId, testProfile5.getUserId(), "bla")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberHigherRoleShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organisationId, service, testProfile2, Role.Admin);

    Duration duration =
        assertMonoCompletesWithError(
            createService(testProfile2)
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        token, organisationId, testProfile5.getUserId(), Role.Owner.toString())),
            AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, "", testProfile5.getUserId(), Role.Member.toString())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullOrgIdShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, null, testProfile5.getUserId(), Role.Member.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organisationId, "", Role.Member.toString())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullUserIdShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    token, organisationId, null, Role.Member.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    new Token(""),
                    organisationId,
                    testProfile5.getUserId(),
                    Role.Member.toString())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    null, organisationId, testProfile5.getUserId(), Role.Member.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration =
        assertMonoCompletesWithError(
            service.inviteMember(
                new InviteOrganizationMemberRequest(
                    new Token(null),
                    organisationId,
                    testProfile5.getUserId(),
                    Role.Member.toString())),
            NullPointerException.class);
    assertNotNull(duration);
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
    Duration duration =
        assertMonoCompletesWithError(
            createService(invalidProfile)
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        token, organisationId, testProfile5.getUserId(), Role.Member.toString())),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }
}
