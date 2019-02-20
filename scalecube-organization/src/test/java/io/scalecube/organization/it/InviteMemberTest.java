package io.scalecube.organization.it;

import static io.scalecube.organization.it.TestProfiles.USER_A;
import static io.scalecube.organization.it.TestProfiles.USER_B;
import static io.scalecube.organization.it.TestProfiles.USER_C;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InviteMemberTest extends BaseTest {

  @Test
  @DisplayName(
      "#49 Successful \"member\" invitation to multiple Organizations which belongs to different owners")
  void inviteUserToMultipleOrganizationsByMultipleOwners() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    CreateOrganizationResponse organizationB =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-2", USER_B.getEmail(), tokenB))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_C.getUserId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(USER_C.getUserId()));
            })
        .verifyComplete();

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenB, organizationB.id(), USER_C.getUserId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationB.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_C.getUserId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(USER_C.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#50 Successful invitation of specified member with \"owner\" role to multiple Organizations which belongs to single owner")
  void inviteUserToMultipleOrganizationsBySingleOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA1 =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    CreateOrganizationResponse organizationA2 =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-2", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA1.id(), USER_B.getUserId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA1.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_B.getUserId()), "member is not found in organization");
              assertEquals(Role.Owner.name(), members.get(USER_B.getUserId()));
            })
        .verifyComplete();

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA2.id(), USER_B.getUserId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA2.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_B.getUserId()), "member is not found in organization");
              assertEquals(Role.Owner.name(), members.get(USER_B.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#51 Successful invitation of the \"member\" into specific Organization upon it's existent \"member\" was granted with \"admin\" role")
  void inviteUserToOrganizationByAdmin() {
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
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenB, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_C.getUserId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(USER_C.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#52 Successful invitation of specific member with \"admin\" role into relevant Organization upon it's existent \"member\" was granted with \"owner\" role")
  void inviteUserToOrganizationByOwner() {
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
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenB, organizationA.id(), USER_C.getUserId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_C.getUserId()), "member is not found in organization");
              assertEquals(Role.Admin.name(), members.get(USER_C.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("#53 Ignore to invite the existent \"member\" (duplicate) to the same Organization")
  void inviteExistentUser() {
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
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA.id(), USER_B.getUserId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_B.getUserId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(USER_B.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#55 Fail to invite the user into relevant Organization upon the existing member (requester) got \"member\" role permission level")
  void inviteUnauthorizedUser() {
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

    StepVerifier.create(
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenB, organizationA.id(), USER_C.getUserId(), Role.Member.name())))
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
      "#56 Fail to invite the user into relevant Organization upon the existing owner (requester) was removed from own Organization")
  void inviteUserByRemovedOwner() {
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
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name())))
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
      "#57 Fail to invite the user as \"Owner\" into relevant Organization by the existing Admin (requester)")
  void inviteUserAsOwnerByAdmin() {
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
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenB, organizationA.id(), USER_C.getUserId(), Role.Owner.name())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', role: '%s' cannot invite to a higher role: '%s'",
                      USER_B.getUserId(), USER_B.getName(), Role.Admin.name(), Role.Owner.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#58 Fail to invite the user to specific Organization if the token is invalid (expired)")
  void inviteUsingExpiredToken() {
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(USER_A);

    StepVerifier.create(
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenA, "ORG-organization-1", USER_B.getUserId(), Role.Member.name())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidTokenException.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
