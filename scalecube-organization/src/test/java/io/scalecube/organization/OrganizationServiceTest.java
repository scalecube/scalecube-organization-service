package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import reactor.test.StepVerifier;

public class OrganizationServiceTest extends Base {

 
  @Test
  public void deleteOrganizationInvalidTokenShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile)
        .deleteOrganization(
        new DeleteOrganizationRequest(token, organisationId)), InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithNullIdShouldFailWithNullPointerException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, null)),
          NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(new Token(""), organisationId)),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganization() {
    String id = createRandomOrganization();
    Duration duration = StepVerifier
        .create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.deleted(), is(true)))
        .expectComplete()
        .verify();
    deleteOrganization(id);
    assertNotNull(duration);
  }
  
 @Test
  public void getOrganizationMembers() {
    addMemberToOrganization(organisationId, service, testProfile4);
    addMemberToOrganization(organisationId, service, testProfile5);

    Duration duration = StepVerifier.create(service.getOrganizationMembers(
        new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
          assertThat(r.members().length, is(3));
          long membersCount = members.get()
              .filter((m) -> Objects.equals(m.role(), Role.Member.toString())).count();
          assertThat(membersCount, is(2L));
          List<String> ids = members.get().map(OrganizationMember::id).collect(Collectors.toList());
          assertThat(ids, hasItem(testProfile4.getUserId()));
          assertThat(ids, hasItem(testProfile5.getUserId()));
        }).verifyComplete();
    assertNotNull(duration);
  }








  @Test
  public void getOrganizationMembersEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest("", token))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(null, token))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            new Token("")))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            null))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            new Token(null)))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile)
            .getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(
            "orgNotExists", token))
        , EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember() {
    addMemberToOrganization(organisationId, service, testProfile2);
    Duration duration = StepVerifier.create(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());

          assertThat(members.get().map(OrganizationMember::id).collect(Collectors.toList()),
              hasItem(testProfile2.getUserId()));
          assertThat(members.get().filter(i -> Objects.equals(i.id(), testProfile2.getUserId()))
              .findFirst().orElseThrow(IllegalStateException::new), is(notNullValue()));

        })
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullRoleShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), null)),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyRoleShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), "")),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberInvalidRoleShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), "bla")),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberHigherRoleShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organisationId, service, testProfile2, Role.Admin);

    Duration duration = expectError(createService(testProfile2).inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), Role.Owner.toString())),
      AccessPermissionException.class);
    assertNotNull(duration);
  }



  @Test
  public void inviteMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "",
          testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, null,
          testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          "", Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullUserIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          null, Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(""), organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(null, organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(null), organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "orgNotExists",
            testProfile5.getUserId(), Role.Member.toString())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMemberShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          testProfile5.getUserId(), Role.Member.toString())),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration = StepVerifier
        .create(createService(testProfile5).leaveOrganization(
            new LeaveOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> assertThat(Arrays.asList(r.members()),
                not(hasItem(
                    new OrganizationMember(testProfile5.getUserId(), Role.Member.toString())))))
            .verifyComplete())
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullTokenIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithEmptyTokenIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(new Token(""), organisationId)),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullInnerTokenIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(new Token(null), organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "orgNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).leaveOrganization(
        new LeaveOrganizationRequest(token, "orgNotExists")),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }
}