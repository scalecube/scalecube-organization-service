package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.NotAnOrganizationMemberException;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class UpdateOrganizationTest extends Base {

  @Test
  void updateOrganizationWithExistingOrgNameShouldFailWithNameAlreadyInUseException() {
    Organization localOrganization = createOrganization(randomString());
    assertMonoCompletesWithError(
        service.updateOrganization(
            new UpdateOrganizationRequest(
                organizationId, token, localOrganization.name(), "update@email")),
        NameAlreadyInUseException.class);
  }

  @Test
  void updateOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.updateOrganization(
            new UpdateOrganizationRequest("orgNotExists", token, "update_name", "update@email")),
        EntityNotFoundException.class);
  }

  @Test
  void updateOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.updateOrganization(
            new UpdateOrganizationRequest("", token, "update_name", "update@email")),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationWithInvalidTokenShouldFailWithInvalidToken() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, token, "update_name", "update@email")),
        InvalidAuthenticationToken.class);
  }

  @Test
  void updateOrganizationWithNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(invalidProfile)
            .updateOrganization(
                new UpdateOrganizationRequest(organizationId, null, "update_name", "update@email")),
        NullPointerException.class);
  }

  @Test
  void updateOrganizationWithNullNameShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .updateOrganization(
                new UpdateOrganizationRequest(organizationId, token, null, "update@email")),
        NullPointerException.class);
  }

  @Test
  void updateOrganizationWithNullEmailShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .updateOrganization(new UpdateOrganizationRequest(organizationId, token, "name", null)),
        NullPointerException.class);
  }

  @Test
  void updateOrganizationWithEmptyNameShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .updateOrganization(
                new UpdateOrganizationRequest(organizationId, token, "", "update@email")),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationWithEmptyEmailShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        createService(testProfile)
            .updateOrganization(new UpdateOrganizationRequest(organizationId, token, "name", "")),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationNotAMemberShouldFail() {
    assertMonoCompletesWithError(
        createService(testProfile5)
            .updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, token, "update_name", "update@email")),
        AccessPermissionException.class);
  }

  @Test
  void updateOrganizationNotAdminShouldFail() {
    orgMembersRepository.addMember(
        getOrganizationFromRepository(organizationId),
        new OrganizationMember(testProfile2.getUserId(), Role.Member.toString()));
    assertMonoCompletesWithError(
        createService(testProfile2)
            .updateOrganization(
                new UpdateOrganizationRequest(
                    organizationId, token, "update_name", "update@email")),
        AccessPermissionException.class);
  }

  @Test
  void updateOrganization() {
    orgMembersRepository.addMember(
        getOrganizationFromRepository(organizationId),
        new OrganizationMember(testAdminProfile.getUserId(), Role.Admin.toString()));

    StepVerifier.create(
            service.addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token, organizationId, "testApiKey", new HashMap<>())))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    StepVerifier.create(
            createService(testAdminProfile)
                .updateOrganization(
                    new UpdateOrganizationRequest(
                        organizationId, token, "update_name", "update@email")))
        .expectSubscription()
        .assertNext(
            (r) -> {
              assertThat("name not updated", r.name(), equalTo("update_name"));
              assertThat("email not updated", r.email(), equalTo("update@email"));
              assertThat("missing api key ", r.apiKeys().length, not(equalTo(0)));
            })
        .verifyComplete();
  }

  @Test
  void updateOrganizationMemberRole() {
    addMemberToOrganization(organizationId, testProfile5);
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Admin.toString())))
        .expectSubscription()
        .assertNext(
            x ->
                StepVerifier.create(
                        createService(testProfile5)
                            .getOrganizationMembers(
                                new GetOrganizationMembersRequest(organizationId, token)))
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
  }

  @Test
  void updateOrganizationMemberRoleNotMemberShouldFail() {
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Admin.toString())))
        .expectSubscription()
        .verifyError(NotAnOrganizationMemberException.class);
  }

  @Test
  void updateOrganizationMemberRoleNotaSuperUserShouldFail() {
    addMemberToOrganization(organizationId, testProfile5);
    addMemberToOrganization(organizationId, testProfile2);
    assertMonoCompletesWithError(
        createService(testProfile2)
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Admin.toString())),
        AccessPermissionException.class);
  }

  @Test
  void updateOrganizationMemberRoleCallerNotOwnerTryingToPromoteToOwnerShouldFail() {
    addMemberToOrganization(organizationId, testProfile5);
    addMemberToOrganization(organizationId, testProfile2);

    // upgrade to admin
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile2.getUserId(), Role.Admin.toString())))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    assertMonoCompletesWithError(
        createService(testProfile2)
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Owner.toString())),
        AccessPermissionException.class);
  }

  @Test
  void updateOrganizationMemberRoleCallerNotOwnerTryingToDowngradeUserShouldFail() {
    addMemberToOrganization(organizationId, testProfile5);
    addMemberToOrganization(organizationId, testProfile2);

    // upgrade to owner

    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Owner.toString())))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    // upgrade to admin
    StepVerifier.create(
            service.updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile2.getUserId(), Role.Admin.toString())))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    // admin tries to downgrade an owner should fail
    assertMonoCompletesWithError(
        createService(testProfile2)
            .updateOrganizationMemberRole(
                new UpdateOrganizationMemberRoleRequest(
                    token, organizationId, testProfile5.getUserId(), Role.Admin.toString())),
        AccessPermissionException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithNullUserIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organizationId, null, Role.Admin.toString())),
        NullPointerException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithEmptyUserIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organizationId, "", Role.Admin.toString())),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithNullOrgIdShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, null, testProfile5.getUserId(), Role.Admin.toString())),
        NullPointerException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, "", testProfile5.getUserId(), Role.Admin.toString())),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithNonExistOrgShouldFailWithEntityNotFoundException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, "bla", testProfile5.getUserId(), Role.Admin.toString())),
        EntityNotFoundException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithNullTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                null, organizationId, testProfile5.getUserId(), Role.Admin.toString())),
        NullPointerException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithNullInnerTokenShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                new Token(null), organizationId, testProfile5.getUserId(), Role.Admin.toString())),
        NullPointerException.class);
  }

  @Test
  void
      updateOrganizationMemberRoleWithEmptyInnerTokenShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                new Token(""), organizationId, testProfile5.getUserId(), Role.Admin.toString())),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithEmptyRoleShouldFailWithIllegalArgumentException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organizationId, testProfile5.getUserId(), "")),
        IllegalArgumentException.class);
  }

  @Test
  void updateOrganizationMemberRoleWithNullRoleShouldFailWithNullPointerException() {
    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organizationId, testProfile5.getUserId(), null)),
        NullPointerException.class);
  }

  @Test
  void
      updateOrganizationMemberRoleInvalidRoleEnumValueShouldFailWithIllegalArgumentException() {
    addMemberToOrganization(organizationId, testProfile5);

    assertMonoCompletesWithError(
        service.updateOrganizationMemberRole(
            new UpdateOrganizationMemberRoleRequest(
                token, organizationId, testProfile5.getUserId(), "invalid role enum value")),
        IllegalArgumentException.class);
  }
}
