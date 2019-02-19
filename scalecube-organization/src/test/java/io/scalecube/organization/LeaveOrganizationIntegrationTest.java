package io.scalecube.organization;

import io.scalecube.account.api.CreateOrganizationRequest;
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
import java.time.Instant;
import java.util.Date;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/** @see features/mpa-7603-Organization-service-Leave-Organization.feature */
class LeaveOrganizationIntegrationTest {

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
  @DisplayName("#MPA-7603 (#28) Owner successfully leaved a relevant Organization")
  void testLeaveOrganizationByOwner() {
    Profile userA = TestProfiles.USER_1;
    Profile userB = TestProfiles.USER_2;
    Token userAToken = MockPublicKeyProvider.token(userA);

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "A" invites user "B" to his organization with an "owner" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Owner.name()))
        .block(TIMEOUT);

    // the user "A" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName("#MPA-7603 (#29) Admin successfully leaved a relevant Organization")
  void testLeaveOrganizationByAdmin() {
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

    // the user "A" invites user "B" to his organization with an "admin" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Admin.name()))
        .block(TIMEOUT);

    // the user "B" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userBToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName("#MPA-7603 (#30) Member successfully leaved a relevant Organization")
  void testLeaveOrganizationByMember() {
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

    // the user "A" invites user "B" to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.getUserId(), Role.Member.name()))
        .block(TIMEOUT);

    // the user "B" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userBToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify(TIMEOUT);
  }

  @Test
  @Disabled // todo need to implement such a behavior
  @DisplayName("#MPA-7603 (#31) Fail to leave a relevant Organization by the single owner")
  void testFailToLeaveOrganizationBySingleOwner() {
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

    // the user "A" requested to leave its organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(
            String.format(
                "At least one Owner should be persisted in the organization: '%s'", organizationId))
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName(
      "#MPA-7603 (#32) Fail to leave the Organization upon the user wasn't invited to any of the relevant Organizations")
  void testFailToLeaveOrganizationWithNonMember() {
    Profile userA = TestProfiles.USER_1;
    Token userAToken = MockPublicKeyProvider.token(userA);
    Profile userB = TestProfiles.USER_2;

    // create a single organization which will be owned by user "A"
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(
                    RandomStringUtils.randomAlphabetic(10), userA.getEmail(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // the user "B" requested to leave a difference organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectNextCount(1)
        .expectComplete()
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName("#MPA-7603 (#33) Fail to leave a non-existent Organization")
  void testFailToLeaveNonExistingOrganization() {
    Profile userA = TestProfiles.USER_1;
    Token userAToken = MockPublicKeyProvider.token(userA);

    String organizationId = "non-existing organization id";

    // the user "A" requests to get info of non-existing organization
    StepVerifier.create(
            service.leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(organizationId)
        .verify(TIMEOUT);
  }

  @Test
  @DisplayName("#MPA-7603 (#34) Fail to leave the Organization if the token is invalid (expired)")
  void testFailToLeaveOrganizationWithExpiredToken() {
    Token expiredToken =
        MockPublicKeyProvider.token(
            TestProfiles.USER_1, op -> op.setExpiration(Date.from(Instant.ofEpochMilli(0))));

    // the user "A" requests to get info with expired token
    StepVerifier.create(
            service.leaveOrganization(
                new LeaveOrganizationRequest(expiredToken, "non-existing-id")))
        .expectErrorMessage("Token verification failed")
        .verify(TIMEOUT);
  }
}
