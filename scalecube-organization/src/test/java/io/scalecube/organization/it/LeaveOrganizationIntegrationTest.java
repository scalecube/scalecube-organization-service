package io.scalecube.organization.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.fixtures.InMemoryOrganizationServiceFixture;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.time.Duration;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Leave-Organization.feature */
@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryOrganizationServiceFixture.class)
class LeaveOrganizationIntegrationTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#28) Owner successfully leaved a relevant Organization")
  void testLeaveOrganizationByOwner(OrganizationService service) {
    Profile userA = TestProfiles.USER_A;
    Profile userB = TestProfiles.USER_B;
    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // the user "A" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#29) Admin successfully leaved a relevant Organization")
  void testLeaveOrganizationByAdmin(OrganizationService service) {
    Profile userA = TestProfiles.USER_A;
    Profile userB = TestProfiles.USER_B;
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

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    // the user "B" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userBToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#30) Member successfully leaved a relevant Organization")
  void testLeaveOrganizationByMember(OrganizationService service) {
    Profile userA = TestProfiles.USER_A;
    Profile userB = TestProfiles.USER_B;
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

    // the user "A" invites user "B" to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // the user "B" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userBToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#31) Fail to leave a relevant Organization by the single owner")
  void testFailToLeaveOrganizationBySingleOwner(OrganizationService service) {
    Profile userA = TestProfiles.USER_A;
    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(
            String.format(
                "At least one Owner should be persisted in the organization: '%s'", organizationId))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7603 (#32) Fail to leave the Organization upon the user wasn't invited to any of the relevant Organizations")
  void testFailToLeaveOrganizationWithNonMember(OrganizationService service) {
    Token userAToken = InMemoryPublicKeyProvider.token(TestProfiles.USER_A);
    Token userBToken = InMemoryPublicKeyProvider.token(TestProfiles.USER_B);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10),
                    TestProfiles.USER_A.email(),
                    userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "B" requested to leave a difference organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userBToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#33) Fail to leave a non-existent Organization")
  void testFailToLeaveNonExistingOrganization(OrganizationService service) {
    Profile userA = TestProfiles.USER_A;
    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    String organizationId = "NON_EXISTING_ID";

    // the user "A" requests to get info of non-existing organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(OrganizationNotFoundException.class, e.getClass());
              assertEquals(
                  String.format("Organization [id=%s] not found", organizationId), e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7603 (#34) Fail to leave the Organization if the token is invalid (expired)")
  void testFailToLeaveOrganizationWithExpiredToken(OrganizationService service) {
    Token expiredToken = InMemoryPublicKeyProvider.expiredToken(TestProfiles.USER_A);

    // the user "A" requests to get info with expired token
    StepVerifier.create(
            service.leaveOrganization(
                new LeaveOrganizationRequest(expiredToken, "non-existing-id")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
