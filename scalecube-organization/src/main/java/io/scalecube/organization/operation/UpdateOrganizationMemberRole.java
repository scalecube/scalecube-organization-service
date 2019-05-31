package io.scalecube.organization.operation;

import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationMemberRoleResponse;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.security.api.Profile;
import reactor.core.publisher.Mono;

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
      TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected Token getToken(UpdateOrganizationMemberRoleRequest request) {
    return request.token();
  }

  @Override
  protected Mono<UpdateOrganizationMemberRoleResponse> process(
      UpdateOrganizationMemberRoleRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(
            organization -> {
              organization.updateMemberRole(request.userId(), Role.valueOf(request.role()));
            })
        .flatMap(organization -> context.repository().save(organization.id(), organization))
        .map(organization -> new UpdateOrganizationMemberRoleResponse());
  }

  @Override
  protected Mono<Void> validate(
      UpdateOrganizationMemberRoleRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () -> {
          requireNonNullOrEmpty(request.userId(), "user id is a required argument");
          requireNonNullOrEmpty(request.role(), "role is a required argument");
          requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
        })
        .then(getOrganization(request.organizationId()))
        .doOnNext(
            organization -> {
              Profile caller = context.profile();
              Role callerRole = getRole(context.profile().userId(), organization);

              checkIsMember(request.userId(), organization);
              checkSuperUserAccess(organization, caller);
              checkIfRequestToUpdateUserRoleIsValidForCaller(
                  toRole(request.role()), context.profile(), callerRole);
              checkIfAdminCallerIsTryingToDowngradeAnOwner(
                  caller, callerRole, organization, request);
              checkLastOwner(request.userId(), organization);
            })
        .then();
  }

  private void checkIfRequestToUpdateUserRoleIsValidForCaller(
      Role targetRole, Profile profile, Role callerRole) throws AccessPermissionException {
    if (targetRole.isHigherThan(callerRole)) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', role: '%s'," + " cannot promote to a higher role: '%s'",
              profile.userId(), profile.name(), callerRole.toString(), targetRole.toString()));
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
              caller.userId(),
              caller.name(),
              callerRole.toString(),
              request.userId(),
              updateUserCurrentRole.toString()));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private TokenVerifier tokenVerifier;
    private OrganizationsRepository repository;

    public UpdateOrganizationMemberRole.Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public UpdateOrganizationMemberRole.Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public UpdateOrganizationMemberRole build() {
      return new UpdateOrganizationMemberRole(tokenVerifier, repository);
    }
  }
}
