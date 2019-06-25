package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

public class KickoutMemberScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#59 Successful kick-out (remove) of specific \"member\" from a relevant Organization")
  void kickoutMemberByOwner(OrganizationService organizationService) {
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
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenA, userB.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(userB.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#60 Successful kick-out (remove) the \"owner\" and \"member\" from relevant Organization by another owner")
  void kickoutMemberAndOwnerByOwner(OrganizationService organizationService) {
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

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, userA.userId()))
                .then(
                    organizationService.kickoutMember(
                        new KickoutOrganizationMemberRequest(
                            organizationA.id(), tokenB, userC.userId())))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(userA.userId()), "member is found in organization");
              assertFalse(members.contains(userC.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#61 Successful kick-out (remove) of the \"admin\" and \"member\" from relevant Organization by another \"admin\"")
  void kickoutByAdmin(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();
    Profile userD = generateProfile();

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

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userD.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, userC.userId()))
                .then(
                    organizationService.kickoutMember(
                        new KickoutOrganizationMemberRequest(
                            organizationA.id(), tokenB, userD.userId())))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(userC.userId()), "member is found in organization");
              assertFalse(members.contains(userD.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#62 Successful kick-out (remove) one of the \"admin\" from relevant Organization by \"owner\"")
  void kickoutAdminByOwner(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), userC.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, userC.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(userC.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#63 Successful kick-out (remove) yourself as the \"owner\" from relevant Organization upon at least one another owner is persisted")
  void kickoutYourselfByOwner(OrganizationService organizationService) {
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
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenA, userA.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenB))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(userA.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#64 Successful kick-out (remove) yourself as the \"admin\" from relevant Organization")
  void kickoutYourselfByAdmin(OrganizationService organizationService) {
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
                .kickoutMember(
                    new KickoutOrganizationMemberRequest(
                        organizationA.id(), tokenB, userB.userId()))
                .then(
                    organizationService.getOrganizationMembers(
                        new GetOrganizationMembersRequest(organizationA.id(), tokenA))))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertFalse(members.contains(userB.userId()), "member is found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#67 Fail to kick-out (remove) yourself as the single \"owner\" from relevant Organization")
  void kickoutSingleOwnerByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, userA.userId())))
        .expectErrorMessage(
            String.format(
                "At least one Owner should be persisted in the organization: '%s'",
                organizationA.id()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#68 Fail to kick-out (remove) the single owner from relevant Organization by the \"admin\"")
  void kickoutSingleOwnerByAdmin(OrganizationService organizationService) {
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
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, userA.userId())))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', role: 'Admin' cannot kickout user: '%s' in role 'Owner' of organization: '%s'",
                userB.userId(), userB.name(), userA.userId(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#69 Fail to kick-out (remove) specific member from relevant Organization upon the existing member (requester) got \"member\" role permission level")
  void kickoutMemberByMember(OrganizationService organizationService) {
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

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, userC.userId())))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#70 Fail to remove a specific \"member\" from relevant Organization upon some of the existing (requester) managers was removed from the relevant organization")
  void kickoutByMember(OrganizationService organizationService) {
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

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenB, userA.userId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, userB.userId())))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userA.userId(), userA.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#71 Fail to remove the user from specific Organization if the token is invalid (expired)")
  void kickoutUsingExpiredToken(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.expiredToken(userA);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest("ORG-organization-1", tokenA, userB.userId())))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName("Fail to kick non-existent user member")
  void kickoutNonExistentMember(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
            .block(TIMEOUT);

    StepVerifier.create(
            organizationService.kickoutMember(
                new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, userB.userId())))
        .expectErrorMessage(
            String.format(
                "user: %s is not a member of organization: %s", userB.userId(), organizationA.id()))
        .verify();
  }
}
