package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

public class GetUserMembershipScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#98 Successful get the list of all Organizations (Membership) in each the user became a Member")
  void getUserMembershipOfMember(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);
    Token tokenC = InMemoryPublicKeyProvider.token(userC);

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

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "ownerApiKeyA",
                Collections.singletonMap("role", Role.Owner.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "adminApiKeyA",
                Collections.singletonMap("role", Role.Admin.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenB,
                organizationB.id(),
                "adminApiKeyB",
                Collections.singletonMap("role", Role.Admin.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenB,
                organizationB.id(),
                "memberApiKeyB",
                Collections.singletonMap("role", Role.Member.name())))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenB, organizationB.id(), userC.userId(), Role.Member.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getUserOrganizationsMembership(new GetMembershipRequest(tokenC)))
        .assertNext(
            response -> {
              Map<String, List<ApiKey>> apiKeys =
                  Stream.of(response.organizations())
                      .collect(
                          Collectors.toMap(
                              OrganizationInfo::id, info -> Arrays.asList(info.apiKeys())));

              assertEquals(
                  0, apiKeys.get(organizationA.id()).size(), "api keys count in the response");

              assertTrue(
                  apiKeys.get(organizationB.id()).stream()
                      .allMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))),
                  "only 'Member' api keys are in the response");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#99 Successful get the list of all Organizations (Membership) in each the user became an Admin")
  void getUserMembershipOfAdmin(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Profile userC = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenB = InMemoryPublicKeyProvider.token(userB);
    Token tokenC = InMemoryPublicKeyProvider.token(userC);

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

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "ownerApiKeyA",
                Collections.singletonMap("role", Role.Owner.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "adminApiKeyA",
                Collections.singletonMap("role", Role.Admin.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenB,
                organizationB.id(),
                "adminApiKeyB",
                Collections.singletonMap("role", Role.Admin.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenB,
                organizationB.id(),
                "memberApiKeyB",
                Collections.singletonMap("role", Role.Member.name())))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userC.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenB, organizationB.id(), userC.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getUserOrganizationsMembership(new GetMembershipRequest(tokenC)))
        .assertNext(
            response -> {
              Map<String, List<ApiKey>> apiKeys =
                  Stream.of(response.organizations())
                      .collect(
                          Collectors.toMap(
                              OrganizationInfo::id, info -> Arrays.asList(info.apiKeys())));

              assertTrue(
                  apiKeys.get(organizationA.id()).stream()
                      .allMatch(
                          apiKey -> {
                            String role = apiKey.claims().get("role");
                            return Role.Admin.name().equals(role);
                          }),
                  "only 'Admin' api keys are in the response");

              assertTrue(
                  apiKeys.get(organizationB.id()).stream()
                      .allMatch(
                          apiKey -> {
                            String role = apiKey.claims().get("role");
                            return Role.Admin.name().equals(role)
                                || Role.Member.name().equals(role);
                          }),
                  "only 'Admin' and 'Member' api keys are in the response");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#100 Successful get the list of all Organizations (Membership) in each the user became an Owner")
  void getUserMembershipOfOwner(OrganizationService organizationService) {
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
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "ownerApiKeyA",
                Collections.singletonMap("role", Role.Owner.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "adminApiKeyA",
                Collections.singletonMap("role", Role.Admin.name())))
        .block(TIMEOUT);

    organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                tokenA,
                organizationA.id(),
                "memberApiKeyA",
                Collections.singletonMap("role", Role.Member.name())))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenA, organizationA.id(), userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getUserOrganizationsMembership(new GetMembershipRequest(tokenB)))
        .assertNext(
            response -> {
              Map<String, List<ApiKey>> apiKeys =
                  Stream.of(response.organizations())
                      .collect(
                          Collectors.toMap(
                              OrganizationInfo::id, info -> Arrays.asList(info.apiKeys())));

              assertEquals(
                  3, apiKeys.get(organizationA.id()).size(), "api keys count in the response");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#101 Do not get any Organization data upon the user hasn't became a member (wasn't invited) to any of the relevant Organizations")
  void getUserMembershipOfNotInvitedMember(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.token(userA);
    Token tokenC = InMemoryPublicKeyProvider.token(userB);

    organizationService
        .createOrganization(
            new CreateOrganizationRequest(
                RandomStringUtils.randomAlphabetic(10), userA.email(), tokenA))
        .block(TIMEOUT);

    StepVerifier.create(
            organizationService.getUserOrganizationsMembership(new GetMembershipRequest(tokenC)))
        .assertNext(
            response ->
                assertEquals(
                    0, response.organizations().length, "organizations count in the response"))
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#102 Fail to get the Membership in Organizations upon the token is invalid (expired)")
  void getUserMembershipUsingExpiredToken(OrganizationService organizationService) {
    Profile userA = generateProfile();
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(userA);

    StepVerifier.create(
            organizationService.getUserOrganizationsMembership(new GetMembershipRequest(tokenA)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
