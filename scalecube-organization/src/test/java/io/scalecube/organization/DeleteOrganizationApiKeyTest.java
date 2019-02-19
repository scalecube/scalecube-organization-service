package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
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
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Add-Api-Key.feature */
class DeleteOrganizationApiKeyTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

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
      "#MPA-7603 (#43) Successful delete any of accessible API key (token) roles from relevant Organization by Owner")
  void testDeleteApiKeysByOwner() {
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
    Flux.just(Role.Owner, Role.Member, Role.Admin)
        .map(
            role ->
                new AddOrganizationApiKeyRequest(
                    userAToken,
                    organizationId,
                    role.name() + "-api-key",
                    Collections.singletonMap("role", role.name())))
        .flatMap(service::addOrganizationApiKey)
        .then()
        .block(TIMEOUT);

    StepVerifier.create(
            Flux.just(Role.Owner, Role.Admin)
                .map(role -> role.name() + "-api-key")
                .map(
                    apiKey ->
                        new DeleteOrganizationApiKeyRequest(userAToken, organizationId, apiKey))
                .flatMap(request -> service.deleteOrganizationApiKey(request)))
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
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify(TIMEOUT);

    // the relevant API keys should be stored in the DB
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .assertNext(
            response -> {
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Member.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify(TIMEOUT);
  }
}
