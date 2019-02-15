package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
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
import io.scalecube.tokens.TokenVerifier;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-UT-Get-Organization */
class UtGetOrganizationTest {

  private OrganizationService service;

  @BeforeEach
  void beforeEach() {
    UserOrganizationMembershipRepository orgMembersRepository =
        new InMemoryUserOrganizationMembershipRepository();
    Repository<Organization, String> organizationRepository = new InMemoryOrganizationRepository();
    OrganizationMembersRepositoryAdmin admin = new InMemoryOrganizationMembersRepositoryAdmin();

    TokenVerifier tokenVerifier = new TestTokenVerifier();

    service =
        OrganizationServiceImpl.builder()
            .organizationRepository(organizationRepository)
            .organizationMembershipRepository(orgMembersRepository)
            .organizationMembershipRepositoryAdmin(admin)
            .tokenVerifier(tokenVerifier)
            .keyPairGenerator(TestHelper.KEY_PAIR_GENERATOR)
            .build();
  }

  @AfterAll
  static void afterAll() {
    new File("keystore.properties").deleteOnExit();
  }

  @Test
  @DisplayName("#MPA-7603 (#1) Successful info get about relevant Organization by the Owner")
  void testGetOrganizationInfoByOwner() {
    Profile userA = TestTokenVerifier.USER_1;
    Profile userB = TestTokenVerifier.USER_2;
    Token userAToken = TestTokenVerifier.token(userA);
    Token userBToken = TestTokenVerifier.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TestHelper.TIMEOUT);

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
            .block(TestHelper.TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
        .block(TestHelper.TIMEOUT);

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
        .verify(TestHelper.TIMEOUT);
  }

  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName("#MPA-7603 (#2) Successful info get about relevant Organization by the Admin")
  void testGetOrganizationInfoByAdmin() {
    Profile userA = TestTokenVerifier.USER_1;
    Profile userB = TestTokenVerifier.USER_2;
    Token userAToken = TestTokenVerifier.token(userA);
    Token userBToken = TestTokenVerifier.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TestHelper.TIMEOUT);

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
            .block(TestHelper.TIMEOUT);

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Admin.name()))
        .block(TestHelper.TIMEOUT);

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
        .verify(TestHelper.TIMEOUT);
  }

  @Test
  @Disabled // todo need to implement this behavior
  @DisplayName("#MPA-7603 (#3) Successful info get about relevant Organization by the Member")
  void testGetOrganizationInfoByMember() {
    Profile userA = TestTokenVerifier.USER_1;
    Profile userB = TestTokenVerifier.USER_2;
    Token userAToken = TestTokenVerifier.token(userA);
    Token userBToken = TestTokenVerifier.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(new CreateOrganizationRequest("repo", userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TestHelper.TIMEOUT);

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
            .block(TestHelper.TIMEOUT);

    // the user "A" invites user "B" to his organization with a "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Member.name()))
        .block(TestHelper.TIMEOUT);

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
        .verify(TestHelper.TIMEOUT);
  }
}
