package io.scalecube.organization.scenario;

import static io.scalecube.organization.scenario.TestProfiles.generateProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.security.api.Profile;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

/** @see features/mpa-7657-Delete-organization.feature */
public class DeleteOrganizationScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#MPA-7657 (#6) Scenario: Successful delete of specific Organization")
  void testOrganizationDeletion(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    StepVerifier.create(
            service.deleteOrganization(new DeleteOrganizationRequest(userAToken, organizationId)))
        .assertNext(
            organization -> {
              assertTrue(organization.deleted());
              assertNotNull(organization.organizationId());
              assertEquals(organizationId, organization.organizationId());
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#7) Scenario: Successful delete of the Organization upon it's \"member\" was granted with owner role")
  void testOrganizationDeletionWithGrantedMember(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create organization "A" user owned
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // "A" user invites "B" user to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // "A" user updates the "B" user to "owner" in his organization
    service
        .updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // "B" user deletes "A" user created organization
    StepVerifier.create(
            service.deleteOrganization(new DeleteOrganizationRequest(userBToken, organizationId)))
        .assertNext(
            organization -> {
              assertTrue(organization.deleted());
              assertNotNull(organization.organizationId());
              assertEquals(organizationId, organization.organizationId());
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#8) Scenario: Fail to delete a specific Organization upon the origin owner was removed from own Organization")
  void testFailOrganizationDeletionWithRemovedFromOwnOrganizationUser(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();
    Token userAToken = InMemoryPublicKeyProvider.token(userA);

    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create organization "A" user owned
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // "A" user invites "B" user to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Owner.name()))
        .block(TIMEOUT);

    // "A" user leaves own organization
    service
        .leaveOrganization(new LeaveOrganizationRequest(userAToken, organizationId))
        .block(TIMEOUT);

    // "A" user deletes organization
    StepVerifier.create(
            service.deleteOrganization(new DeleteOrganizationRequest(userAToken, organizationId)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', is not in role Owner of organization: '%s'",
                userA.name(), userA.userId(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#9) Scenario: Fail to delete the Organization upon it's \"member\" was granted with admin role permission level")
  void testFailOrganizationDeletionWithGrantedAdminRoleMember(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create organization "A" user owned
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // "A" user invites "B" user to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // "A" user updates the "B" user to "owner" in his organization
    service
        .updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                userAToken, organizationId, userB.userId(), Role.Admin.name()))
        .block(TIMEOUT);

    // "B" user deletes organization
    StepVerifier.create(
            service.deleteOrganization(new DeleteOrganizationRequest(userBToken, organizationId)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', is not in role Owner of organization: '%s'",
                userB.name(), userB.userId(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#10) Scenario: Fail to delete the Organization upon the relevant member got the \"member\" role permission level")
  void testFailOrganizationDeletionWithGrantedMemberRoleMember(OrganizationService service) {
    Profile userA = generateProfile();
    Profile userB = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    Token userBToken = InMemoryPublicKeyProvider.token(userB);

    String organizationName = RandomStringUtils.randomAlphabetic(10);

    // create organization "A" user owned
    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // "A" user invites "B" user to his organization with an "member" role
    service
        .inviteMember(
            new InviteOrganizationMemberRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // "A" user updates the "B" user to "owner" in his organization
    service
        .updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                userAToken, organizationId, userB.userId(), Role.Member.name()))
        .block(TIMEOUT);

    // "B" user deletes organization
    StepVerifier.create(
            service.deleteOrganization(new DeleteOrganizationRequest(userBToken, organizationId)))
        .expectErrorMessage(
            String.format(
                "user: '%s', name: '%s', is not in role Owner of organization: '%s'",
                userB.name(), userB.userId(), organizationName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#MPA-7657 (#11) Scenario: Fail to delete the Organization if the token is invalid (expired)")
  void testFailOrganizationDeletionWithExpiredToken(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);

    String organizationId =
        service
            .createOrganization(
                new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
            .map(OrganizationInfo::id)
            .block(TIMEOUT);

    // create organization with invalid token
    StepVerifier.create(
            service.deleteOrganization(
                new DeleteOrganizationRequest(new Token("invalid"), organizationId)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName("#MPA-7657 (#12) Scenario: Fail to delete a non-existent Organization")
  void testFailOrganizationDeletionNonExistingOrganization(OrganizationService service) {
    Profile userA = generateProfile();

    Token userAToken = InMemoryPublicKeyProvider.token(userA);
    String organizationName = RandomStringUtils.randomAlphabetic(10);
    String organizationNameNotExisting = RandomStringUtils.randomAlphabetic(10);

    service
        .createOrganization(
            new CreateOrganizationRequest(organizationName, userA.email(), userAToken))
        .map(OrganizationInfo::id)
        .block(TIMEOUT);

    // create organization with invalid token
    StepVerifier.create(
            service.deleteOrganization(
                new DeleteOrganizationRequest(userAToken, organizationNameNotExisting)))
        .expectErrorMessage(
            String.format("Organization [id=%s] not found", organizationNameNotExisting))
        .verify();
  }
}
