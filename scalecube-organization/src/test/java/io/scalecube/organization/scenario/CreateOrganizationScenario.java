package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

/** @see features/mpa-7657-Create-organization.feature */
public class CreateOrganizationScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#MPA-7657 (#1) Scenario: Successful creation of the Organization")
  void testOrganizationCreation(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken)))
        .assertNext(
            organization -> {
              assertNotNull(organization.id());
              assertTrue(organization.id().startsWith("ORG-"));
              assertEquals(organizationName, organization.name());
              assertEquals(userA.email(), organization.email());
              assertNotNull(organization.apiKeys());
              assertEquals(0, organization.apiKeys().length);
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#2) Scenario: Fail to create the Organization if the token is invalid (expired)")
  void testFailOrganizationCreationWithInvalidToken(OrganizationService service) {
    Profile userA = generateProfile();

    // create organization with invalid token
    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(
                    "organizationName", userA.email(), new Token("invalid"))))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#3) Scenario: Fail to create the Organization with the name which already exists (duplicate)")
  void testFailOrganizationCreationWithExistingName(OrganizationService service)
      throws InterruptedException {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    service
        .createOrganization(
            new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
        .block(TIMEOUT);

    TimeUnit.MILLISECONDS.sleep(300);

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken)))
        .expectErrorMessage("Organization name: '" + organizationName + "' already in use")
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7657 (#4) Scenario: Fail to create the Organization without email")
  void testFailOrganizationCreationWithoutEmail(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, null, userAToken)))
        .expectErrorMessage("Organization email cannot be empty")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#5) Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars")
  void testFailOrganizationCreationWithDeniedSymbolsName(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10) + "+";

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken)))
        .expectErrorMessage(
            "Organization name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent")
        .verify();
  }
}
