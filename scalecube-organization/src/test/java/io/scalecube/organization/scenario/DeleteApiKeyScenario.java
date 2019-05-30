package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Delete-Api-Key.feature */
public class DeleteApiKeyScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#43) Successful delete any of accessible API key (token) roles from relevant Organization by Owner")
  void testDeleteApiKeysByOwner(OrganizationService service) {
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

    // the user "A" deletes the API keys which were assigned roles: "owner" and "admin"
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
        .verify();

    // the user "A" verifies remaining API keys with extended organization info
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
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#44) Successful delete the API keys (token) only with admin and member roles from relevant Organization by Admin")
  void testDeleteApiKeysByAdmin(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Profile userB = generateProfile();
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), userAToken))
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

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    // the user "B" deletes the API keys which were assigned roles: "admin" and "member"
    StepVerifier.create(
            Flux.just(Role.Admin, Role.Member)
                .map(role -> role.name() + "-api-key")
                .map(
                    apiKey ->
                        new DeleteOrganizationApiKeyRequest(userBToken, organizationId, apiKey))
                .flatMap(service::deleteOrganizationApiKey))
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
              assertEquals(0, response.apiKeys().length);
            })
        .expectComplete()
        .verify();

    // the user "A" verifies remaining API keys with extended organization info
    StepVerifier.create(
            service.getOrganization(new GetOrganizationRequest(userAToken, organizationId)))
        .assertNext(
            response -> {
              assertEquals(1, response.apiKeys().length);
              assertTrue(
                  Arrays.stream(response.apiKeys())
                      .anyMatch(apiKey -> Role.Owner.name().equals(apiKey.claims().get("role"))));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#46) Fail to delete any of accessible API key (token) roles from relevant Organization by the Member with similar role")
  void testFailToDeleteMemberApiKeysByMemberRole(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Profile userB = generateProfile();
    Token userBToken = InMemoryPublicKeyProvider.token(userB);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with "member" role
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

    // the user "A" invites user "B" to his organization with a "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // the user "B" deletes the API key which was assigned roles "member"
    StepVerifier.create(
            service.deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(
                    userBToken, organizationId, Role.Member.name() + "-api-key")))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#47) Fail to delete non-existent (invalid) API key (token) from specific Organization")
  void testFailToDeleteNonExistingApiKey(OrganizationService service) {
    Profile userA = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" creates API keys for the organization with roles "member"
    String apiKeyName = Role.Member.name() + "-api-key";
    service
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                userAToken,
                organizationId,
                apiKeyName,
                Collections.singletonMap("role", Role.Member.name())))
        .block(TIMEOUT);

    // the user "B" deletes the non-existing API key
    StepVerifier.create(
            service.deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(
                    userAToken, organizationId, "non-existing-api-key")))
        .assertNext(
            response -> {
              assertEquals(organizationId, response.id());
              assertEquals(1, response.apiKeys().length);
              ApiKey apiKey = response.apiKeys()[0];
              assertEquals(apiKeyName, apiKey.name());
              assertEquals(Role.Member.name(), apiKey.claims().get("role"));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#48) Fail to delete the API key (token) from relevant Organization if the token is invalid (expired)")
  void testFailToDeleteApiKeyWithExpiredToken(OrganizationService service) {
    Token expiredToken = InMemoryPublicKeyProvider.expiredToken(generateProfile());

    // the user "A" requests to get info with expired token
    StepVerifier.create(
            service.deleteOrganizationApiKey(
                new DeleteOrganizationApiKeyRequest(expiredToken, "non-existing-id", "some-name")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
