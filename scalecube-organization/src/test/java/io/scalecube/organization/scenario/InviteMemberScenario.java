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
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

public class InviteMemberScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#49 Successful \"member\" invitation to multiple Organizations which belongs to different owners")
  void inviteUserToMultipleOrganizationsByMultipleOwners(OrganizationService organizationService) {
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

    CreateOrganizationResponse organizationB =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userB.email(), tokenB))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA.id(), userC.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userC.userId()), "member is not found in organization");
              assertEquals(Role.Member.name(), members.get(userC.userId()));
            })
        .verifyComplete();

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenB, organizationB.id(), userC.userId(), Role.Member.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationB.id(), tokenB))))
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
      "#50 Successful invitation of specified member with \"owner\" role to multiple Organizations which belongs to single owner")
  void inviteUserToMultipleOrganizationsBySingleOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA1 =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    CreateOrganizationResponse organizationA2 =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA1.id(), userB.userId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA1.id(), tokenA))))
        .assertNext(
            response -> {
              Map<String, String> members =
                  Stream.of(response.members())
                      .collect(Collectors.toMap(OrganizationMember::id, OrganizationMember::role));

              assertNotNull(members.get(userB.userId()), "member is not found in organization");
              assertEquals(Role.Owner.name(), members.get(userB.userId()));
            })
        .verifyComplete();

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
                        tokenA, organizationA2.id(), userB.userId(), Role.Owner.name()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA2.id(), tokenA))))
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
      "#51 Successful invitation of the \"member\" into specific Organization upon it's existent \"member\" was granted with \"admin\" role")
  void inviteUserToOrganizationByAdmin(OrganizationService organizationService) {
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

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
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
      "#52 Successful invitation of specific member with \"admin\" role into relevant Organization upon it's existent \"member\" was granted with \"owner\" role")
  void inviteUserToOrganizationByOwner(OrganizationService organizationService) {
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

    StepVerifier.create(
            organizationService
                .inviteMember(
                    new InviteOrganizationMemberRequest(
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
  @DisplayName("#53 Ignore to invite the existent \"member\" (duplicate) to the same Organization")
  void inviteExistentUser(OrganizationService organizationService) {
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
                .inviteMember(
                    new InviteOrganizationMemberRequest(
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
              assertEquals(Role.Member.name(), members.get(userB.userId()));
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#55 Fail to invite the user into relevant Organization upon the existing member (requester) got \"member\" role permission level")
  void inviteUnauthorizedUser(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenB, organizationA.id(), userC.userId(), Role.Member.name())))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#56 Fail to invite the user into relevant Organization upon the existing owner (requester) was removed from own Organization")
  void inviteUserByRemovedOwner(OrganizationService organizationService) {
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
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, userA.userId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenA, organizationA.id(), userC.userId(), Role.Member.name())))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userA.userId(), userA.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#57 Fail to invite the user as \"Owner\" into relevant Organization by the existing Admin (requester)")
  void inviteUserAsOwnerByAdmin(OrganizationService organizationService) {
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

    StepVerifier.create(
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenB, organizationA.id(), userC.userId(), Role.Owner.name())))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', role: '%s' cannot invite to a higher role: '%s'",
                userB.userId(), userB.name(), Role.Admin.name(), Role.Owner.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#58 Fail to invite the user to specific Organization if the token is invalid (expired)")
  void inviteUsingExpiredToken(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.expiredToken(userA);

    StepVerifier.create(
            organizationService.inviteMember(
                new InviteOrganizationMemberRequest(
                    tokenA, "ORG-organization-1", userB.userId(), Role.Member.name())))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
