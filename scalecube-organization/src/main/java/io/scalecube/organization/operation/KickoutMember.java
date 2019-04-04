package io.scalecube.organization.operation;

import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberResponse;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.tokens.TokenVerifier;

public class KickoutMember
    extends ServiceOperation<KickoutOrganizationMemberRequest, KickoutOrganizationMemberResponse> {

  private KickoutMember(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected KickoutOrganizationMemberResponse process(
      KickoutOrganizationMemberRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    checkSuperUserAccess(organization, context.profile());
    ensureCallerIsInHigherRoleThanKickedOutUser(request, context, organization);
    checkLastOwner(request.userId(), organization);

    organization.removeMember(request.userId());
    context.repository().save(organization.id(), organization);

    return new KickoutOrganizationMemberResponse();
  }

  private void ensureCallerIsInHigherRoleThanKickedOutUser(
      KickoutOrganizationMemberRequest request,
      OperationServiceContext context,
      Organization organization)
      throws EntityNotFoundException, AccessPermissionException {

    Role callerRole = getRole(context.profile().getUserId(), organization);
    Role targetRole = getRole(request.userId(), organization);

    if (targetRole.isHigherThan(callerRole)) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', role: '%s' cannot kickout "
                  + "user: '%s' in role '%s' of organization: '%s'",
              context.profile().getUserId(),
              context.profile().getName(),
              callerRole,
              request.userId(),
              targetRole,
              organization.name()));
    }
  }

  @Override
  protected void validate(KickoutOrganizationMemberRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
    requireNonNullOrEmpty(request.userId(), "user id is required");
  }

  @Override
  protected Token getToken(KickoutOrganizationMemberRequest request) {
    return request.token();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private TokenVerifier tokenVerifier;
    private OrganizationsRepository repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public KickoutMember build() {
      return new KickoutMember(tokenVerifier, repository);
    }
  }
}
