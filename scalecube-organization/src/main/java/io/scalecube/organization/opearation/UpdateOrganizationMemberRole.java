package io.scalecube.organization.opearation;

import io.scalecube.account.api.NotAnOrganizationMemberException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationMemberRoleResponse;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import io.scalecube.tokens.TokenVerifier;

/**
 * Encapsulates the processing of a request to update the role of an organization member. This
 * operation is only permitted to super users of the organization (users in role Owner|Admin). In
 * case an admin user is trying to promote user to become an owner, an exception will be thrown. In
 * case an admin user is trying to downgrade a owner, an exception will be thrown.
 */
public class UpdateOrganizationMemberRole
    extends ServiceOperation<
        UpdateOrganizationMemberRoleRequest, UpdateOrganizationMemberRoleResponse> {

  private UpdateOrganizationMemberRole(
      TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected Token getToken(UpdateOrganizationMemberRoleRequest request) {
    return request.token();
  }

  @Override
  protected UpdateOrganizationMemberRoleResponse process(
      UpdateOrganizationMemberRoleRequest request, OperationServiceContext context) {
    Organization organization = getOrganization(request.organizationId());
    context
        .repository()
        .updateOrganizationMemberRole(organization, request.userId(), request.role());

    return new UpdateOrganizationMemberRoleResponse();
  }

  @Override
  protected void validate(
      UpdateOrganizationMemberRoleRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.userId(), "user id is a required argument");
    requireNonNullOrEmpty(request.role(), "role is a required argument");
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");

    Organization organization = getOrganization(request.organizationId());
    Profile caller = context.profile();
    Role callerRole = getRole(context.profile().getUserId(), organization);

    checkIsMember(request.userId(), context, organization);
    checkSuperUserAccess(organization, caller);
    checkIfRequestToUpdateUserRoleIsValidForCaller(
        toRole(request.role()), context.profile(), callerRole);
    checkIfAdminCallerIsTryingToDowngradeAnOwner(caller, callerRole, organization, request);
    checkLastOwner(request.userId(), organization);
  }

  private void checkIfRequestToUpdateUserRoleIsValidForCaller(
      Role targetRole, Profile profile, Role callerRole) throws AccessPermissionException {
    if (targetRole.isHigherThan(callerRole)) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', role: '%s'," + " cannot promote to a higher role: '%s'",
              profile.getUserId(),
              profile.getName(),
              callerRole.toString(),
              targetRole.toString()));
    }
  }

  private void checkIfAdminCallerIsTryingToDowngradeAnOwner(
      Profile caller,
      Role callerRole,
      Organization organization,
      UpdateOrganizationMemberRoleRequest request)
      throws AccessPermissionException, EntityNotFoundException {
    Role updateUserCurrentRole = getRole(request.userId(), organization);

    if (updateUserCurrentRole.isHigherThan(callerRole)) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', role: %s,"
                  + " cannot downgrade user id: %s, in higher role: '%s'.",
              caller.getUserId(),
              caller.getName(),
              callerRole.toString(),
              request.userId(),
              updateUserCurrentRole.toString()));
    }
  }

  private void checkIsMember(
      String userId, OperationServiceContext context, Organization organization)
      throws NotAnOrganizationMemberException {
    if (!context.repository().isMember(userId, organization)) {
      throw new NotAnOrganizationMemberException(
          String.format(
              "user: %s, is not a member of organization: %s", userId, organization.id()));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private TokenVerifier tokenVerifier;
    private OrganizationsDataAccess repository;

    public UpdateOrganizationMemberRole.Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public UpdateOrganizationMemberRole.Builder repository(OrganizationsDataAccess repository) {
      this.repository = repository;
      return this;
    }

    public UpdateOrganizationMemberRole build() {
      return new UpdateOrganizationMemberRole(tokenVerifier, repository);
    }
  }
}
