package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
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
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Add-Api-Key.feature */
public class AddApiKeyScenario extends BaseScenario {

  private static final Random RANDOM = new Random(42);

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#35) Successful adding the API keys (token) for relevant Organization with all accessible roles by Owner")
  void testAddAllApiKeysForEachAccessibleRoleOfTheOwner(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Owner + "-api-key",
                    Collections.singletonMap("role", Role.Owner.name()))))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Member + "-api-key",
                    Collections.singletonMap("role", Role.Member.name()))))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(2, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Admin + "-api-key",
                    Collections.singletonMap("role", Role.Admin.name()))))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(3, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Admin.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();

    // the user "A" verifies stored API keys with extended organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .assertNext(
            response -> {
              assertEquals(3, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Admin.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#36) Successful adding the API keys (token) with admin and member roles for relevant Organization by Admin")
  void testAddAllApiKeysForEachAccessibleRoleOfTheAdmin(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner"
    service
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                userAToken,
                organizationId,
                Role.Owner.name() + "-api-key",
                Collections.singletonMap("role", Role.Owner.name())))
        .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    // user "B" creates API keys for the organization with roles: "admin" and "member"
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userBToken,
                    organizationId,
                    Role.Member + "-api-key",
                    Collections.singletonMap("role", Role.Member.name()))))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userBToken,
                    organizationId,
                    Role.Admin + "-api-key",
                    Collections.singletonMap("role", Role.Admin.name()))))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(2, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Admin.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();

    // the user "A" verifies stored API keys with extended organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .assertNext(
            response -> {
              assertEquals(3, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Admin.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#37) Fail to add the owner API key (token) for a relevant Organization by the Admin")
  void testFailToAddOwnerApiKeyByAdmin(OrganizationService service) {
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

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    // user "B" creates API keys for the organization with roles: "owner"
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userBToken,
                    organizationId,
                    Role.Owner.name() + "-api-key",
                    Collections.singletonMap("role", Role.Owner.name()))))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', role: 'Admin' cannot add api key with higher role 'Owner'",
                userB.userId(), userB.name()))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#38) Fail to add some of accessible API keys (token) for a relevant Organization upon the relevant member doesn't manager's permission level")
  void testFailToAddMemberApiKeyByMember(OrganizationService service) {
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

    // the user "A" invites user "B" to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // user "B" creates API keys for the organization with roles: "member"
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userBToken,
                    organizationId,
                    Role.Member.name() + "-api-key",
                    Collections.singletonMap("role", Role.Member.name()))))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#39) Fail to add some of accessible API keys (token) with the duplicate name for a relevant Organization")
  void testFailToAddApiKeyWithDuplicatedName(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String specifiedApiKeyName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner"
    service
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                userAToken,
                organizationId,
                specifiedApiKeyName,
                Collections.singletonMap("role", Role.Owner.name())))
        .block(TIMEOUT);

    // user "A" creates API keys for the organization with any accessible role
    Role role = Role.values()[RANDOM.nextInt(Role.values().length)];
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    specifiedApiKeyName,
                    Collections.singletonMap("role", role.name()))))
        .expectErrorMessage(String.format("apiKey name:'%s' already exists", specifiedApiKeyName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#40) Fail to add some of accessible API keys (token) for relevant Organization upon the owner was removed from own Organization")
  void testFailToAddApiKeyAfterOwnerWasRemoved(OrganizationService service) {
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

    // the user "A" invites user "B" to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // the user "A" leaves own organization
    service
        .leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId))
        .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "admin"
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    Role.Admin.name() + "-api-key",
                    Collections.singletonMap("role", Role.Admin.name()))))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userA.userId(), userA.name(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#41) Fail to add the API key (token) for a relevant Organization upon the assigned role is invalid (differs from allowed)")
  void testFailToAddApiKeWithInvalidRole(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String invalidRole = "boss";

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with invalid role
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    invalidRole + "-api-key",
                    Collections.singletonMap("role", invalidRole))))
        .expectErrorMessage(String.format("Role '%s' is invalid", invalidRole))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#42) Fail to add the API key (token) for a relevant Organization if the token is invalid (expired)")
  void testFailToAddApiKeWithInvalidToken(OrganizationService service) {
    // user "A" creates API keys for the organization with invalid token
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    new Token("invalid"),
                    RandomStringUtils.randomAlphabetic(10),
                    Role.Member.name() + "-api-key",
                    Collections.singletonMap("role", Role.Member.name()))))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
