package io.scalecube.organization;

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
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.security.Profile;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Add-Api-Key.feature */
class AddOrganizationApiKeyIntegrationTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);
  private static final Random RANDOM = new Random(42);

  private OrganizationService service;

  @BeforeEach
  void beforeEach() {
    UserOrganizationMembershipRepository orgMembersRepository =
        new InMemoryUserOrganizationMembershipRepository();
    Repository<Organization, String> organizationRepository = new InMemoryOrganizationRepository();
    OrganizationMembersRepositoryAdmin admin = new InMemoryOrganizationMembersRepositoryAdmin();

    service =
        OrganizationServiceImpl.builder()
            .organizationRepository(organizationRepository)
            .organizationMembershipRepository(orgMembersRepository)
            .organizationMembershipRepositoryAdmin(admin)
            .keyPairGenerator(MockPublicKeyProvider.KPG)
            .build();
  }

  @AfterAll
  static void afterAll() {
    new File("keystore.properties").deleteOnExit();
  }

  @Test
  @DisplayName(
      "#MPA-7603 (#35) Successful adding the API keys (token) for relevant Organization with all accessible roles by Owner")
  void testAddAllApiKeysForEachAccessibleRoleOfTheOwner() {
    Profile userA = TestProfiles.USER_1;
    Token userAToken = MockPublicKeyProvider.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    StepVerifier.create(
            Flux.just(Role.Owner, Role.Member, Role.Admin)
                .map(
                    role ->
                        new AddOrganizationApiKeyRequest(
                            userAToken,
                            organizationId,
                            role.name() + "-api-key",
                            Collections.singletonMap("role", role.name())))
                .flatMap(service::addOrganizationApiKey))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
            })
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
        .verify(TIMEOUT);

    // the relevant API keys should be stored in the DB
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
        .verify(TIMEOUT);
  }

  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName(
      "#MPA-7603 (#36) Successful adding the API keys (token) with admin and member roles for relevant Organization by Admin")
  void testAddAllApiKeysForEachAccessibleRoleOfTheAdmin() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Token userBToken = MockPublicKeyProvider.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.getEmail(), userAToken))
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
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
        .block(TIMEOUT);

    // user "B" creates API keys for the organization with roles: "admin" and "member"
    StepVerifier.create(
            Flux.just(Role.Member, Role.Admin)
                .map(
                    role ->
                        new AddOrganizationApiKeyRequest(
                            userBToken,
                            organizationId,
                            role.name() + "-api-key",
                            Collections.singletonMap("role", role.name())))
                .flatMap(service::addOrganizationApiKey))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
            })
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
        .verify(TIMEOUT);

    // the relevant API keys should be stored in the DB
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
        .verify(TIMEOUT);
  }

  /**
   * todo ask about {@link
   * io.scalecube.organization.opearation.ServiceOperation#checkSuperUserAccess(io.scalecube.organization.Organization,
   * io.scalecube.security.Profile)}
   */
  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName(
      "#MPA-7603 (#37) Fail to add the owner API key (token) for a relevant Organization by the Admin")
  void testFailToAddOwnerApiKeyByAdmin() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Token userBToken = MockPublicKeyProvider.token(userB);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
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
                "user: '%s', name: '%s', not in role Owner of organization: '%s'",
                userB.getUserId(), userB.getName(), organizationName))
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName(
      "#MPA-7603 (#38) Fail to add some of accessible API keys (token) for a relevant Organization upon the relevant member doesn't manager's permission level")
  void testFailToAddMemberApiKeyByMember() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Token userBToken = MockPublicKeyProvider.token(userB);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Member.name()))
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
                userB.getUserId(), userB.getName(), organizationName))
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName(
      "#MPA-7603 (#39) Fail to add some of accessible API keys (token) with the duplicate name for a relevant Organization")
  void testFailToAddApiKeyWithDuplicatedName() {
    Profile userA = TestProfiles.USER_1;
    Token userAToken = MockPublicKeyProvider.token(userA);
    String specifiedApiKeyName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.getEmail(), userAToken))
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
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName(
      "#MPA-7603 (#40) Fail to add some of accessible API keys (token) for relevant Organization upon the owner was removed from own Organization")
  void testFailToAddApiKeyAfterOwnerWasRemoved() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
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
                userA.getUserId(), userA.getName(), organizationName))
        .verify(TIMEOUT);
  }

  /**
   *
   *
   * <pre>
   *   Caused by: java.lang.UnsupportedOperationException
   * 	at java.util.AbstractMap.put(AbstractMap.java:209) ~[?:1.8.0_152]
   * 	at io.scalecube.tokens.store.ApiKeyBuilder.build(ApiKeyBuilder.java:36) ~[classes/:?]
   * 	at io.scalecube.organization.opearation.AddOrganizationApiKey.process(AddOrganizationApiKey.java:27) ~[classes/:?]
   * 	at io.scalecube.organization.opearation.AddOrganizationApiKey.process(AddOrganizationApiKey.java:14) ~[classes/:?]
   * 	at io.scalecube.organization.opearation.ServiceOperation.execute(ServiceOperation.java:49) ~[classes/:?]
   * </pre>
   */
  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName(
      "#MPA-7603 (#41) Fail to add the API key (token) for a relevant Organization upon the assigned role is invalid (differs from allowed)")
  void testFailToAddApiKeWithInvalidRole() {
    Profile userA = TestProfiles.USER_1;
    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String invalidRole = "boss";

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken))
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
        .expectErrorMessage(String.format("role: '%s' is invalid", invalidRole))
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName(
      "#MPA-7603 (#42) Fail to add the API key (token) for a relevant Organization if the token is invalid (expired)")
  void testFailToAddApiKeWithInvalidToken() {
    // user "A" creates API keys for the organization with invalid token
    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    new Token("invalid"),
                    RandomStringUtils.randomAlphabetic(10),
                    Role.Member.name() + "-api-key",
                    Collections.singletonMap("role", Role.Member.name()))))
        .expectErrorMessage("Token verification failed")
        .verify(TIMEOUT);
  }
}
