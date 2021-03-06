package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Update-Organization.feature */
public class UpdateOrganizationScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#MPA-7603 (#19) Successful update of the relevant Organization by the Owner")
  void testUpdateOrganizationByOwner(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String newOrganizationName = "new" + organizationName;
    String newEmail = "new" + userA.email();

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

    // user "A" updates repo name and email in the organization
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, userAToken, newOrganizationName, newEmail)))
        .assertNext(
            organization -> {
              assertEquals(organizationId, organization.id());
              assertEquals(newOrganizationName, organization.name());
              assertEquals(newEmail, organization.email());
              // user "A" should get all stored API keys
              assertEquals(apiKeys, new HashSet<>(Arrays.asList(organization.apiKeys())));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#20) Successful update of the relevant Organization by the Admin")
  void testUpdateOrganizationByAdmin(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String newOrganizationName = "new" + organizationName;
    String newEmail = "new" + userA.email();

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

    // user "B" updates repo name and email in the organization
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, userBToken, newOrganizationName, newEmail)))
        .assertNext(
            organization -> {
              assertNotNull(organization);
              assertEquals(organizationId, organization.id());
              assertEquals(newOrganizationName, organization.name());
              assertEquals(newEmail, organization.email());
              // user "B" should get only stored "admin" and "member" API keys
              assertEquals(2, organization.apiKeys().length);
              assertEquals(apiKeys, new HashSet<>(Arrays.asList(organization.apiKeys())));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#21) Successful update of the Organization upon it's member was granted with Owner role")
  void testUpdateOrganizationByMember(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String newOrganizationName = "new" + organizationName;
    String newEmail = "new" + userA.email();

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

    // the user "A" updates the user "B" role to "owner" in the own organization
    service
        .updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // user "B" updates repo name and email in the organization
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, userBToken, newOrganizationName, newEmail)))
        .assertNext(
            organization -> {
              assertNotNull(organization);
              assertEquals(organizationId, organization.id());
              assertEquals(newOrganizationName, organization.name());
              assertEquals(newEmail, organization.email());
              // user "B" should get only stored "admin" and "member" API keys
              assertEquals(0, organization.apiKeys().length);
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#22) Fail to update relevant Organization by the Member with similar role")
  void testFailToUpdateOrganizationMemberByMember(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String newOrganizationName = "new" + organizationName;
    String newEmail = "new" + userA.email();

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

    // user "B" updates repo name and email in the organization
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, userBToken, newOrganizationName, newEmail)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userB.userId(), userB.name(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#23) Fail to update relevant Organization upon the Owner was removed from it")
  void testFailToUpdateOrganizationBecauseOwnerWasRemoved(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String newOrganizationName = "new" + organizationName;
    String newEmail = "new" + userA.email();

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

    // user "A" updates repo name and email in the organization
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, userAToken, newOrganizationName, newEmail)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
                userA.userId(), userA.name(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#24) Fail to update the Organization with the name which already exists (duplicate)")
  void testFailToUpdateOrganizationBecauseNameIsDuplicated(OrganizationService service)
      throws InterruptedException {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String userAOrganizationName = RandomStringUtils.randomAlphabetic(10);
    String userBOrganizationName = RandomStringUtils.randomAlphabetic(10);

    // create a single organization which will be owned by user "A"
    String userAOrganizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(userAOrganizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // create a single organization which will be owned by user "B"
    service
        .createOrganization(
            new CreateOrganizationRequest(userBOrganizationName, userB.email(), userBToken))
        .map(OrganizationInfo::id)
        .block(TIMEOUT);

    TimeUnit.MILLISECONDS.sleep(300);

    // user "A" updates the name of own organization using the organization name of user "B"
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    userAOrganizationId, userAToken, userBOrganizationName, userA.email())))
        .expectErrorMessage(
            String.format("Organization name: '%s' already in use", userBOrganizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#25) Fail to update the non-existent Organization")
  void testFailToUpdateNonExistingOrganization(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    String nonExistingOrganizationId = RandomStringUtils.randomAlphabetic(10);

    // user "A" updates the name and email of non-existent organization
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    nonExistingOrganizationId, userAToken, "fictionalName", userA.email())))
        .expectErrorMessage(
            String.format("Organization [id=%s] not found", nonExistingOrganizationId))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#26) Fail to update the Organization with the name which contain else symbols apart of allowed chars")
  void testFailToUpdateOrganizationNameWithNotAllowedSymbols(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String incorrectName = organizationName + "+";

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // user "A" updates organization with the incorrect name
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, userAToken, incorrectName, userA.email())))
        .expectErrorMessage(
            "Organization name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent")
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#27) Fail to update the Organization if the token is invalid (expired)")
  void testFailToUpdateOrganizationWithInvalidToken(OrganizationService service) {
    Profile userA = generateProfile();

    // user "A" updates organization with invalid token
    StepVerifier.create(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    "organizationId", new Token("invalid"), "name", userA.email())))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
