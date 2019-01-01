package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.NotAnOrganizationMemberException;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class UpdateOrganizationTest extends Base {

  /**
   * #MPA-7229 (#1.2) Scenario: Fail to create the Organization with the name which already exists
   * (duplicate) Given the user "A" have got a valid "token" issued by relevant authority And the
   * organization "organizationId" with specified "name" and "email" already created and owned by
   * user "B" When the user "A" requested to create the organization with the existent user's "B"
   * organization "name" and some or the same "email" Then user "A" should get an error message:
   * "Organization name: 'org "B" name' already in use"
   */
  @Test
  public void updateOrganizationWithExistingOrgNameShouldFailWithNameAlreadyInUseException() {
    Organization localOrganization = createOrganization(randomString());

    Duration duration =
        expectError(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    organisationId, token, localOrganization.name(), "update@email")),
            NameAlreadyInUseException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration =
        expectError(
            service.updateOrganization(
                new UpdateOrganizationRequest(
                    "orgNotExists", token, "update_name", "update@email")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.updateOrganization(
                new UpdateOrganizationRequest("", token, "update_name", "update@email")),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithInvalidTokenShouldFailWithInvalidToken() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(
                        organisationId, token, "update_name", "update@email")),
            InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            createService(invalidProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(
                        organisationId, null, "update_name", "update@email")),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithNullNameShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(organisationId, token, null, "update@email")),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithNullEmailShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(organisationId, token, "name", null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithEmptyNameShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(organisationId, token, "", "update@email")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationWithEmptyEmailShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            createService(testProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(organisationId, token, "name", "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationNotAMemberShouldFail() {
    expectError(
        createService(testProfile5)
            .updateOrganization(
                new UpdateOrganizationRequest(
                    organisationId, token, "update_name", "update@email")),
        AccessPermissionException.class);
  }

  @Test
  public void updateOrganizationNotAdminShouldFail() {
    orgMembersRepository.addMember(
        getOrganizationFromRepository(organisationId),
        new OrganizationMember(testProfile2.getUserId(), Role.Member.toString()));
    expectError(
        createService(testProfile2)
            .updateOrganization(
                new UpdateOrganizationRequest(
                    organisationId, token, "update_name", "update@email")),
        AccessPermissionException.class);
  }

  @Test
  public void updateOrganization() {
    orgMembersRepository.addMember(
        getOrganizationFromRepository(organisationId),
        new OrganizationMember(testAdminProfile.getUserId(), Role.Admin.toString()));
    consume(
        service.addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(
                token, organisationId, "testApiKey", new HashMap<>())));
    Duration duration =
        StepVerifier.create(
                createService(testAdminProfile)
                    .updateOrganization(
                        new UpdateOrganizationRequest(
                            organisationId, token, "update_name", "update@email")))
            .expectSubscription()
            .assertNext(
                (r) -> {
                  assertThat("name not updated", r.name(), is("update_name"));
                  assertThat("email not updated", r.email(), is("update@email"));
                  assertThat("missing api key ", r.apiKeys().length, is(not(0)));
                })
            .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRole() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration =
        StepVerifier.create(
                service.updateOrganizationMemberRole(
                    new UpdateOrganizationMemberRoleRequest(
                        token, organisationId, testProfile5.getUserId(), Role.Admin.toString())))
            .expectSubscription()
            .assertNext(
                x ->
                    StepVerifier.create(
                            createService(testProfile5)
                                .getOrganizationMembers(
                                    new GetOrganizationMembersRequest(organisationId, token)))
                        .expectSubscription()
                        .assertNext(
                            r ->
                                assertTrue(
                                    Arrays.stream(r.members())
                                        .anyMatch(
                                            i ->
                                                Objects.equals(i.id(), testProfile5.getUserId())
                                                    && Objects.equals(
                                                        i.role(), Role.Admin.toString())))))
            .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleNotMemberShouldFail() {
    expectError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        NotAnOrganizationMemberException.class);
  }

  @Test
  public void updateOrganizationMemberRoleNotaSuperUserShouldFail() {
    addMemberToOrganization(organisationId, service, testProfile5);
    addMemberToOrganization(organisationId, service, testProfile2);
    expectError(
        createService(testProfile2)
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void updateOrganizationMemberRoleCallerNotOwnerTryingToPromoteToOwnerShouldFail() {
    addMemberToOrganization(organisationId, service, testProfile5);
    addMemberToOrganization(organisationId, service, testProfile2);

    // upgrade to admin
    consume(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organisationId, testProfile2.getUserId(), Role.Admin.toString())));
    expectError(
        createService(testProfile2)
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, testProfile5.getUserId(), Role.Owner.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void updateOrganizationMemberRoleCallerNotOwnerTryingTo_downgradeUserShouldFail() {
    addMemberToOrganization(organisationId, service, testProfile5);
    addMemberToOrganization(organisationId, service, testProfile2);

    // upgrade to owner
    consume(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organisationId, testProfile5.getUserId(), Role.Owner.toString())));
    // upgrade to admin
    consume(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organisationId, testProfile2.getUserId(), Role.Admin.toString())));

    // admin tries to downgrade an owner should fail
    expectError(
        createService(testProfile2)
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
        AccessPermissionException.class);
  }

  @Test
  public void updateOrganizationMemberRoleWithNullUserIdShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, null, Role.Admin.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithEmptyUserIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, "", Role.Admin.toString())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithNullOrgIdShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, null, testProfile5.getUserId(), Role.Admin.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, "", testProfile5.getUserId(), Role.Admin.toString())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithNonExistOrgShouldFailWithEntityNotFoundException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, "bla", testProfile5.getUserId(), Role.Admin.toString())),
            EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithNullTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    null, organisationId, testProfile5.getUserId(), Role.Admin.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    new Token(null),
                    organisationId,
                    testProfile5.getUserId(),
                    Role.Admin.toString())),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void
      updateOrganizationMemberRoleWithEmptyInnerTokenShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    new Token(""),
                    organisationId,
                    testProfile5.getUserId(),
                    Role.Admin.toString())),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithEmptyRoleShouldFailWithIllegalArgumentException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, testProfile5.getUserId(), "")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void updateOrganizationMemberRoleWithNullRoleShouldFailWithNullPointerException() {
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, testProfile5.getUserId(), null)),
            NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void
      updateOrganizationMemberRoleInvalidRoleEnumValueShouldFailWithIllegalArgumentException() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration =
        expectError(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organisationId, testProfile5.getUserId(), "invalid role enum value")),
            IllegalArgumentException.class);
    assertNotNull(duration);
  }
}