package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

public class UpdateMemberRoleScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#72 Successful upgrade of specific \"member\" to \"admin\" role in the relevant Organization by the \"owner\"")
  void updateMemberToAdminByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Admin.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#73 Successful upgrade of specific \"member\" to \"owner\" role in the relevant Organization by another \"owner\"")
  void updateMemberToOwnerByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Owner.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#74 Successful upgrade of \"admin\" to \"owner\" role in the relevant Organization by another \"owner\"")
  void updateAdminToOwnerByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Owner.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#75 Successful upgrade of specific \"member\" to \"admin\" role in the relevant Organization by the \"admin\"")
  void updateMemberToAdminByAdmin(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenB, organizationA.id(), userC.userId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userC.userId()), "member is not found in organization");
              assertEquals(Role.Admin.name(), members.get(userC.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#76 Successful downgrade of the \"owner\" to \"admin\" role in the relevant Organization by another \"owner\"")
  void updateOwnerToAdminByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Admin.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#77 Successful downgrade of the \"owner\" to \"member\" role in the relevant Organization by another \"owner\"")
  void updateOwnerToMemberByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#78 Successful downgrade of the \"admin\" to \"member\" role in the relevant Organization by the \"owner\"")
  void updateAdminToMemberByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#79 Successful downgrade \"admin\" to \"member\" role in the relevant Organization by another \"admin\"")
  void updateAdminToMemberByAdmin(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenB, organizationA.id(), userC.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userC.userId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(userC.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#80 Successful downgrade yourself as the \"owner\" to \"member\" either \"admin\" role in the relevant Organization")
  void updateOwnerToMemberByYourself(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userA.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userA.userId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(userA.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#81 Successful downgrade yourself as \"admin\" to \"member\" role in the relevant Organization")
  void updateAdminToMemberByYourself(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenB, organizationA.id(), userB.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#87 Fail to downgrade yourself as the single \"owner\" to \"member\" either \"admin\" role in the relevant Organization")
  void updateLastOwnerToMember(OrganizationService organizationService) {
    Profile userA = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userA.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .expectErrorMessage(
            String.format(
                "At least one Owner should be persisted in the organization: '%s'",
                organizationA.id()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#88 Fail to upgrade a \"member\" either \"admin\" to \"owner\" role in the relevant Organization by the \"admin\"")
  void updateMemberToOwnerByAdmin(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenC = InMemoryPublicKeyProvider.token(userC);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenC, organizationA.id(), userB.userId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', role: '%s', cannot promote to a higher role: '%s'",
                userC.userId(), userC.name(), Role.Admin.name(), Role.Owner.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#89 Fail to update any accessible member role in the relevant Organization by the \"member\"")
  void updateByMember(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenC = InMemoryPublicKeyProvider.token(userC);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenC, organizationA.id(), userB.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userC.userId(), userC.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#90 Fail to update any accessible member role in the relevant Organization by the owner who was removed from own Organization")
  void updateByRemovedOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, userA.userId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        tokenA, organizationA.id(), userC.userId(), Role.Admin.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userA.userId(), userA.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#91 Fail to update some member role in some Organization if the token is invalid (expired)")
  void updateUsingExpiredToken(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.expiredToken(userA);

    StepVerifier.create(
            organizationService.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    tokenA, "ORG-organizatnio-1", userB.userId(), Role.Member.name())))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
