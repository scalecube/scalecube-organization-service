package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryOrganizationServiceFixture;
import io.scalecube.organization.repository.exception.InvalidInputException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.security.Profile;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import io.scalecube.tokens.InvalidTokenException;
import java.time.Duration;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

/** @see features/mpa-7657-Create-organization.feature */
@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryOrganizationServiceFixture.class)
public class CreateOrganizationIntegrationTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  @TestTemplate
  @DisplayName("#MPA-7657 (#1) Scenario: Successful creation of the Organization")
  void testOrganizationCreation(OrganizationService service) {
    Profile userA = TestProfiles.USER_1;

    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken)))
        .assertNext(
            organization -> {
              assertNotNull(organization.id());
              assertTrue(organization.id().startsWith("ORG-"));
              assertEquals(organizationName, organization.name());
              assertEquals(userA.getEmail(), organization.email());
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
    Profile userA = TestProfiles.USER_1;

    // create organization with invalid token
    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(
                    "organizationName", userA.getEmail(), new Token("invalid"))))
        .expectErrorMatches(
            ex ->
                ex instanceof InvalidTokenException
                    && ex.getMessage().equals("Token verification failed"))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#3) Scenario: Fail to create the Organization with the name which already exists (duplicate)")
  void testFailOrganizationCreationWithExistingName(OrganizationService service) {
    Profile userA = TestProfiles.USER_1;

    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    service
        .createOrganization(
            new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken))
        .map(OrganizationInfo::id)
        .block(TIMEOUT);

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken)))
        .expectErrorMatches(
            ex ->
                ex instanceof NameAlreadyInUseException
                    && ex.getMessage()
                        .equals("Organization name: '" + organizationName + "' already in use"))
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7657 (#4) Scenario: Fail to create the Organization without email")
  void testFailOrganizationCreationWithoutEmail(OrganizationService service) {
    Profile userA = TestProfiles.USER_1;

    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, null, userAToken)))
        .expectErrorMatches(
            ex ->
                ex instanceof NullPointerException
                    && ex.getMessage().equals("Organization email cannot be empty"))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#5) Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars")
  void testFailOrganizationCreationWithDeniedSymbolsName(OrganizationService service) {
    Profile userA = TestProfiles.USER_1;

    Token userAToken = MockPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10) + "+";

    StepVerifier.create(
            service.createOrganization(
                new CreateOrganizationRequest(organizationName, userA.getEmail(), userAToken)))
        .expectErrorMatches(
            ex ->
                ex instanceof InvalidInputException
                    && ex.getMessage()
                        .startsWith("Organization name can only contain characters in range"))
        .verify();
  }
}
