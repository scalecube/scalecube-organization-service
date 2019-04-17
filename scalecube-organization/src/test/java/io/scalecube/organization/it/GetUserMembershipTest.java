package io.scalecube.organization.it;

import static io.scalecube.organization.it.TestProfiles.USER_A;
import static io.scalecube.organization.it.TestProfiles.USER_B;
import static io.scalecube.organization.it.TestProfiles.USER_C;
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
import io.scalecube.organization.fixtures.InMemoryOrganizationServiceFixture;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.organization.tokens.InvalidTokenException;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryOrganizationServiceFixture.class)
class GetUserMembershipTest extends BaseTest {

  @TestTemplate
  @DisplayName(
      "#98 Successful get the list of all Organizations (Membership) in each the user became a Member")
  void getUserMembershipOfMember(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);
    Token tokenC = InMemoryPublicKeyProvider.token(USER_C);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    CreateOrganizationResponse organizationB =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_B.email(), tokenB))
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
                tokenA, organizationA.id(), USER_C.userId(), Role.Member.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenB, organizationB.id(), USER_C.userId(), Role.Member.name()))
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
                  apiKeys
                      .get(organizationB.id())
                      .stream()
                      .allMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))),
                  "only 'Member' api keys are in the response");
            })
        .verifyComplete();
  }

  @TestTemplate
  @DisplayName(
      "#99 Successful get the list of all Organizations (Membership) in each the user became an Admin")
  void getUserMembershipOfAdmin(OrganizationService organizationService) {
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);
    Token tokenC = InMemoryPublicKeyProvider.token(USER_C);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
            .block(TIMEOUT);

    CreateOrganizationResponse organizationB =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_B.email(), tokenB))
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
                tokenA, organizationA.id(), USER_C.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    organizationService
        .inviteMember(
            new InviteOrganizationMemberRequest(
                tokenB, organizationB.id(), USER_C.userId(), Role.Admin.name()))
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
                  apiKeys
                      .get(organizationA.id())
                      .stream()
                      .allMatch(
                          apiKey -> {
                            String role = apiKey.claims().get("role");
                            return Role.Admin.name().equals(role);
                          }),
                  "only 'Admin' api keys are in the response");

              assertTrue(
                  apiKeys
                      .get(organizationB.id())
                      .stream()
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
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenB = InMemoryPublicKeyProvider.token(USER_B);

    CreateOrganizationResponse organizationA =
        organizationService
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
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
                tokenA, organizationA.id(), USER_B.userId(), Role.Owner.name()))
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
    Token tokenA = InMemoryPublicKeyProvider.token(USER_A);
    Token tokenC = InMemoryPublicKeyProvider.token(USER_C);

    organizationService
        .createOrganization(
            new CreateOrganizationRequest(
                RandomStringUtils.randomAlphabetic(10), USER_A.email(), tokenA))
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
    Token tokenA = InMemoryPublicKeyProvider.expiredToken(USER_A);

    StepVerifier.create(
            organizationService.getUserOrganizationsMembership(new GetMembershipRequest(tokenA)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidTokenException.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
