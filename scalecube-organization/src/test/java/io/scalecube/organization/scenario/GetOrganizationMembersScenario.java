package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class GetOrganizationMembersScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#92 Successful get all the members list from relevant Organization by the \"owner\"")
  void getMembersByOwner(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    Profile userB = generateProfile();
    Profile userC = generateProfile();

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
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenA)))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertTrue(members.contains(userA.userId()), "member A is not found in organization");
              assertTrue(members.contains(userB.userId()), "member B is not found in organization");
              assertTrue(members.contains(userC.userId()), "member C is not found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#93 Successful get all the members list from relevant Organization by the the \"admin\"")
  void getMembersByAdmin(OrganizationService organizationService) {
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
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenB)))
        .assertNext(
            response -> {
              List<String> members =
                  Stream.of(response.members())
                      .map(OrganizationMember::id)
                      .collect(Collectors.toList());

              assertTrue(members.contains(userA.userId()), "member A is not found in organization");
              assertTrue(members.contains(userB.userId()), "member B is not found in organization");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#94 Fail to get the list of all the members from the relevant Organization by the existing member with similar role")
  void getMembersByMember(OrganizationService organizationService) {
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
                tokenA, organizationA.id(), userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenB)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#95 Fail to get the list of all the members from the relevant Organization upon some of the existing (requester) managers was removed from the relevant organization")
  void getMembersByRemovedAdmin(OrganizationService organizationService) {
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

    organizationService
        .kickoutMember(
            new KickoutOrganizationMemberRequest(organizationA.id(), tokenA, userB.userId()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationA.id(), tokenB)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationA.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName("#96 Fail to get members from non-existent Organization")
  void getMembersFromNonExistentOrganization(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.token(userA);

    String organizationId = RandomStringUtils.randomAlphabetic(10);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationId, tokenA)))
        .expectErrorMessage(String.format("Organization [id=%s] not found", organizationId))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#97 Fail to invite the user to specific Organization if the token is invalid (expired)")
  void getMembersUsingExpiredToken(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(userA);

    String organizationId = RandomStringUtils.randomAlphabetic(10);

    StepVerifier.create(
            organizationService.getOrganizationMembers(
                new GetOrganizationMembersRequest(organizationId, tokenA)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
