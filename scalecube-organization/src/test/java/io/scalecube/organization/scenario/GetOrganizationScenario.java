package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Get-Organization.feature */
public class GetOrganizationScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#MPA-7603 (#13) Successful info get about relevant Organization by the Owner")
  void testGetOrganizationInfoByOwner(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    Set<ApiKey> apiKeys =
        service
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Owner.name() + "-api-key",
                    Collections.singletonMap("role", Role.Owner.name())))
            .concatWith(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        Role.Member.name() + "-api-key",
                        Collections.singletonMap("role", Role.Member.name()))))
            .concatWith(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        Role.Admin.name() + "-api-key",
                        Collections.singletonMap("role", Role.Admin.name()))))
            .map(OrganizationInfo::apiKeys)
            .flatMap(Flux::fromArray)
            .collectList()
            .map(HashSet::new)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // the user "B" requested to get the user's "A" organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userBToken, organizationId)))
        .assertNext(
            organization -> {
              assertNotNull(organization);
              assertEquals(organizationId, organization.id());
              // user "B" should get all stored API keys
              assertEquals(apiKeys, new HashSet<>(Arrays.asList(organization.apiKeys())));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#14) Successful info get about relevant Organization by the Admin")
  void testGetOrganizationInfoByAdmin(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    Set<ApiKey> apiKeys =
        service
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Owner.name() + "-api-key",
                    Collections.singletonMap("role", Role.Owner.name())))
            .concatWith(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        Role.Member.name() + "-api-key",
                        Collections.singletonMap("role", Role.Member.name()))))
            .concatWith(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        Role.Admin.name() + "-api-key",
                        Collections.singletonMap("role", Role.Admin.name()))))
            .map(OrganizationInfo::apiKeys)
            .flatMap(Flux::fromArray)
            // but we need to leave out only "admin" and "member" as the expected result
            .filter(apiKey -> !Role.Owner.name().equals(apiKey.claims().get("role")))
            .collectList()
            .map(HashSet::new)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    // the user "B" requested to get the user's "A" organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userBToken, organizationId)))
        .assertNext(
            organization -> {
              assertNotNull(organization);
              assertEquals(organizationId, organization.id());
              // user "B" should get only stored "admin" and "member" API keys
              assertEquals(apiKeys, new HashSet<>(Arrays.asList(organization.apiKeys())));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#15) Successful info get about relevant Organization by the Member")
  void testGetOrganizationInfoByMember(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    Set<ApiKey> apiKeys =
        service
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Owner.name() + "-api-key",
                    Collections.singletonMap("role", Role.Owner.name())))
            .concatWith(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        Role.Member.name() + "-api-key",
                        Collections.singletonMap("role", Role.Member.name()))))
            .concatWith(
                service.addOrganizationApiKey(
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        Role.Admin.name() + "-api-key",
                        Collections.singletonMap("role", Role.Admin.name()))))
            .map(OrganizationInfo::apiKeys)
            .flatMap(Flux::fromArray)
            // but we need to leave out only "member" as the expected result
            .filter(apiKey -> Role.Member.name().equals(apiKey.claims().get("role")))
            .collectList()
            .map(HashSet::new)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with a "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // the user "B" requested to get the user's "A" organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userBToken, organizationId)))
        .assertNext(
            organization -> {
              assertNotNull(organization);
              assertEquals(organizationId, organization.id());
              // user "B" should get only stored "member" API keys
              assertEquals(apiKeys, new HashSet<>(Arrays.asList(organization.apiKeys())));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#16) Fail to get of specific Organization info upon the Owner was removed from relevant Organization")
  void testFailToGetOrganizationInfoBecauseOwnerWasRemoved(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // the user "A" leaves own organization
    service
        .leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId))
        .block(TIMEOUT);

    // the user "A" requests to get own former organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', is not a member of organization: '%s'",
                userA.name(), userA.userId(), organizationId))
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#17) Fail to get a non-existent Organization info")
  void testFailToGetNonExistingOrganizationInfo(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationId = RandomStringUtils.randomAlphabetic(10);

    // the user "A" requests to get info of non-existing organization
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(String.format("Organization [id=%s] not found", organizationId))
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#18) Fail to get the Organization info if the token is invalid")
  void testFailToGetOrganizationInfoWithInvalidToken(OrganizationService service) {
    Token expiredToken = InMemoryPublicKeyProvider.expiredToken(generateProfile());

    // the user "A" requests to get info with invalid token
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(expiredToken, "non-existing-id")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
