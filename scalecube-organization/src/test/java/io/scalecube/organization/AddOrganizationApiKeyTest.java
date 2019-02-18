package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetOrganizationRequest;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Add-Api-Key.feature */
class AddOrganizationApiKeyTest {

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
  @DisplayName(
      "#MPA-7603 (#35) Successful adding the API keys (token) for relevant Organization with all accessible roles by Owner")
  void testAddAllApiKeysForEachAccessibleRoleOfTheOwner() {
    Profile userA = TestTokenVerifier.USER_1;
    Token userAToken = TestTokenVerifier.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    TestHelper.randomString(10), userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TestHelper.TIMEOUT);

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
        .verify(TestHelper.TIMEOUT);

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
        .verify(TestHelper.TIMEOUT);
  }
}
