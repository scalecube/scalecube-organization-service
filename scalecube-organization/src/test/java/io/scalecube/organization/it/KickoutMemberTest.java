package io.scalecube.organization.it;

import static io.scalecube.organization.it.TestProfiles.USER_A;
import static io.scalecube.organization.it.TestProfiles.USER_B;
import static io.scalecube.organization.it.TestProfiles.USER_C;
import static io.scalecube.organization.it.TestProfiles.USER_D;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.tokens.InvalidTokenException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class KickoutMemberTest extends BaseTest {

  @Test
  @DisplayName(
      "#59 Successful kick-out (remove) of specific \"member\" from a relevant Organization")
  void kickoutMemberByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenA, USER_B.getUserId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_B.getUserId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#60 Successful kick-out (remove) the \"owner\" and \"member\" from relevant Organization by another owner")
  void kickoutMemberAndOwnerByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_A.getUserId()))
                .then(
                    organizationService.kickoutMember(
                        new KickoutOrganizationMemberRequest(
                            organizationA.id(), tokenB, USER_C.getUserId())))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_A.getUserId()), "member is found in organization");
              assertFalse(members.contains(USER_C.getUserId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#61 Successful kick-out (remove) of the \"admin\" and \"member\" from relevant Organization by another \"admin\"")
  void kickoutByAdmin() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_D.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_C.getUserId()))
                .then(
                    organizationService.kickoutMember(
                        new KickoutOrganizationMemberRequest(
                            organizationA.id(), tokenB, USER_D.getUserId())))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_C.getUserId()), "member is found in organization");
              assertFalse(members.contains(USER_D.getUserId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#62 Successful kick-out (remove) one of the \"admin\" from relevant Organization by \"owner\"")
  void kickoutAdminByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_C.getUserId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_C.getUserId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#63 Successful kick-out (remove) yourself as the \"owner\" from relevant Organization upon at least one another owner is persisted")
  void kickoutYourselfByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenA, USER_A.getUserId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_A.getUserId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#64 Successful kick-out (remove) yourself as the \"admin\" from relevant Organization")
  void kickoutYourselfByAdmin() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, USER_B.getUserId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(USER_B.getUserId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#67 Fail to kick-out (remove) yourself as the single \"owner\" from relevant Organization")
  void kickoutSingleOwnerByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenA, USER_A.getUserId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(IllegalStateException.class, e.getClass());
              assertEquals(
                  String.format(
                      "At least one Owner should be persisted in the organization: '%s'",
                      organizationA.id()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#68 Fail to kick-out (remove) the single owner from relevant Organization by the \"admin\"")
  void kickoutSingleOwnerByAdmin() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenB, USER_A.getUserId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', role: 'Admin' cannot kickout user: '%s' in role 'Owner' of organization: '%s'",
                      USER_B.getUserId(),
                      USER_B.getName(),
                      USER_A.getUserId(),
                      organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#69 Fail to kick-out (remove) specific member from relevant Organization upon the existing member (requester) got \"member\" role permission level")
  void kickoutMemberByMember() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenB, USER_C.getUserId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                      USER_B.getUserId(), USER_B.getName(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#70 Fail to remove a specific \"member\" from relevant Organization upon some of the existing (requester) managers was removed from the relevant organization")
  void kickoutByMember() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, USER_A.getUserId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    organizationA.id(), tokenA, USER_B.getUserId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                      USER_A.getUserId(), USER_A.getName(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#71 Fail to remove the user from specific Organization if the token is invalid (expired)")
  void kickoutUsingExpiredToken() {
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(USER_A);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(
                    "ORG-organization-1", tokenA, USER_B.getUserId())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidTokenException.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
