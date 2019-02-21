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
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.tokens.InvalidTokenException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class UpdateMemberRoleTest extends BaseTest {

  @Test
  @DisplayName(
      "#72 Successful upgrade of specific \"member\" to \"admin\" role in the relevant Organization by the \"owner\"")
  void updateMemberToAdminByOwner() {
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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
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
              assertEquals(Role.Admin.name(), members.get(USER_B.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#73 Successful upgrade of specific \"member\" to \"owner\" role in the relevant Organization by another \"owner\"")
  void updateMemberToOwnerByOwner() {
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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
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
      "#74 Successful upgrade of \"admin\" to \"owner\" role in the relevant Organization by another \"owner\"")
  void updateAdminToOwnerByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
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
      "#75 Successful upgrade of specific \"member\" to \"admin\" role in the relevant Organization by the \"admin\"")
  void updateMemberToAdminByAdmin() {
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
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
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
  @DisplayName(
      "#76 Successful downgrade of the \"owner\" to \"admin\" role in the relevant Organization by another \"owner\"")
  void updateOwnerToAdminByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
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
              assertEquals(Role.Admin.name(), members.get(USER_B.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#77 Successful downgrade of the \"owner\" to \"member\" role in the relevant Organization by another \"owner\"")
  void updateOwnerToMemberByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
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
      "#78 Successful downgrade of the \"admin\" to \"member\" role in the relevant Organization by the \"owner\"")
  void updateAdminToMemberByOwner() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
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
      "#79 Successful downgrade \"admin\" to \"member\" role in the relevant Organization by another \"admin\"")
  void updateAdminToMemberByAdmin() {
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

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
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
      "#80 Successful downgrade yourself as the \"owner\" to \"member\" either \"admin\" role in the relevant Organization")
  void updateOwnerToMemberByYourself() {
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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_A.getUserId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(USER_A.getUserId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(USER_A.getUserId()));
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "#81 Successful downgrade yourself as \"admin\" to \"member\" role in the relevant Organization")
  void updateAdminToMemberByYourself() {
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
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenB, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
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
      "#87 Fail to downgrade yourself as the single \"owner\" to \"member\" either \"admin\" role in the relevant Organization")
  void updateLastOwnerToMember() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest("organization-1", USER_A.getEmail(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_A.getUserId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
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
      "#88 Fail to upgrade a \"member\" either \"admin\" to \"owner\" role in the relevant Organization by the \"admin\"")
  void updateMemberToOwnerByAdmin() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenC = InMemoryPublicKeyProvider.token(USER_C);

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
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenC, organizationA.id(), USER_B.getUserId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', role: '%s', cannot promote to a higher role: '%s'",
                      USER_C.getUserId(), USER_C.getName(), Role.Admin.name(), Role.Owner.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#89 Fail to update any accessible member role in the relevant Organization by the \"member\"")
  void updateByMember() {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenC = InMemoryPublicKeyProvider.token(USER_C);

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
                tokenA, organizationA.id(), USER_C.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
        organizationService
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    tokenC, organizationA.id(), USER_B.getUserId(), Role.Member.name()))
            .then(
                organizationService.getOrganizationMembers(
                    new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessPermissionException.class, e.getClass());
              assertEquals(
                  String.format(
                      "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                      USER_C.getUserId(), USER_C.getName(), organizationA.name()),
                  e.getMessage());
            })
        .verify();
  }

  @Test
  @DisplayName(
      "#90 Fail to update any accessible member role in the relevant Organization by the owner who was removed from own Organization")
  void updateByRemovedOwner() {
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

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, USER_A.getUserId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), USER_C.getUserId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
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
      "#91 Fail to update some member role in some Organization if the token is invalid (expired)")
  void updateUsingExpiredToken() {
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(USER_A);

    StepVerifier.create(
            organizationService.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    tokenA, "ORG-organizatnio-1", USER_B.getUserId(), Role.Member.name())))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidTokenException.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
