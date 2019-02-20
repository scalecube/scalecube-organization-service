package io.scalecube.organization;

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
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.security.Profile;
import io.scalecube.tokens.TokenVerifierImpl;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Get-Organization.feature */
class GetOrganizationIntegrationTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private OrganizationService service;

  @BeforeEach
  void beforeEach() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
    UserOrganizationMembershipRepository orgMembersRepository =
        new InMemoryUserOrganizationMembershipRepository();
    Repository<Organization, String> organizationRepository = new InMemoryOrganizationRepository();
    OrganizationMembersRepositoryAdmin admin = new InMemoryOrganizationMembersRepositoryAdmin();

    OrganizationsDataAccess repository =
        OrganizationsDataAccessImpl.builder()
            .organizations(organizationRepository)
            .members(orgMembersRepository)
            .repositoryAdmin(admin)
            .build();
    TokenVerifierImpl tokenVerifier = new TokenVerifierImpl(new MockPublicKeyProvider());
    service = new OrganizationServiceImpl(repository, tokenVerifier);
  }

  @AfterAll
  static void afterAll() {
    new File("keystore.properties").deleteOnExit();
  }

  @Test
  @DisplayName("#MPA-7603 (#13) Successful info get about relevant Organization by the Owner")
  void testGetOrganizationInfoByOwner() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Token userBToken = MockPublicKeyProvider.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    Set<ApiKey> apiKeys =
        Flux.just(Role.Owner, Role.Member, Role.Admin)
            .map(
                role ->
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        role.name() + "-api-key",
                        Collections.singletonMap("role", role.name())))
            .flatMap(service::addOrganizationApiKey)
            .map(OrganizationInfo::apiKeys)
            .flatMap(Flux::fromArray)
            .collectList()
            .map(HashSet::new)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
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

  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName("#MPA-7603 (#14) Successful info get about relevant Organization by the Admin")
  void testGetOrganizationInfoByAdmin() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Token userBToken = MockPublicKeyProvider.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    Set<ApiKey> apiKeys =
        Flux.just(Role.Owner, Role.Member, Role.Admin)
            .map(
                role ->
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        role.name() + "-api-key",
                        Collections.singletonMap("role", role.name())))
            .flatMap(service::addOrganizationApiKey)
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
                userAToken, organizationId, userB.getUserId(), Role.Admin.name()))
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

  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName("#MPA-7603 (#15) Successful info get about relevant Organization by the Member")
  void testGetOrganizationInfoByMember() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Token userBToken = MockPublicKeyProvider.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles: "owner", "admin" and "member"
    Set<ApiKey> apiKeys =
        Flux.just(Role.Owner, Role.Member, Role.Admin)
            .map(
                role ->
                    new AddOrganizationApiKeyRequest(
                        userAToken,
                        organizationId,
                        role.name() + "-api-key",
                        Collections.singletonMap("role", role.name())))
            .flatMap(service::addOrganizationApiKey)
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
                userAToken, organizationId, userB.getUserId(), Role.Member.name()))
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

  @Test
  @DisplayName(
      "#MPA-7603 (#16) Fail to get of specific Organization info upon the Owner was removed from relevant Organization")
  void testFailToGetOrganizationInfoBecauseOwnerWasRemoved() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
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
                userA.getName(), userA.getUserId(), organizationId))
        .verify();
  }

  @Test
  @DisplayName("#MPA-7603 (#17) Fail to get a non-existent Organization info")
  void testFailToGetNonExistingOrganizationInfo() {
    Profile userA = TestProfiles.USER_1;
    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationId = "non-existing organization id";

    // the user "A" requests to get info of non-existing organization
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(organizationId)
        .verify();
  }

  @Test
  @DisplayName("#MPA-7603 (#18) Fail to get the Organization info if the token is invalid")
  void testFailToGetOrganizationInfoWithInvalidToken() {
    Token expiredToken =
        MockPublicKeyProvider.token(
            TestProfiles.USER_1, op -> op.setExpiration(Date.from(Instant.ofEpochMilli(0))));

    // the user "A" requests to get info with invalid token
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(expiredToken, "non-existing-id")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
