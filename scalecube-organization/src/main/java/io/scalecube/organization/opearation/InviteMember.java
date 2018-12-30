package io.scalecube.organization.opearation;

import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;

import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.tokens.TokenVerifier;

public class InviteMember
    extends ServiceOperation<InviteOrganizationMemberRequest, InviteOrganizationMemberResponse> {

  private InviteMember(TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected InviteOrganizationMemberResponse process(
      InviteOrganizationMemberRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    checkSuperUserAccess(organization, context.profile());
    Role invitedMemberRole = toRole(request.role());

    Role callerRole = getRole(context.profile().getUserId(), organization);

    if (RoleRank.from(callerRole).isHigherRank(invitedMemberRole)) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', role: %s cannot invite to a higher role: '%s'",
              context.profile().getUserId(),
              context.profile().getName(),
              callerRole,
              invitedMemberRole.toString()));
    }

    context
        .repository()
        .invite(context.profile(), organization, request.userId(), invitedMemberRole);
    return new InviteOrganizationMemberResponse();
  }

  @Override
  protected void validate(InviteOrganizationMemberRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
    requireNonNullOrEmpty(request.userId(), "user id is required");
  }

  @Override
  protected Token getToken(InviteOrganizationMemberRequest request) {
    return request.token();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private TokenVerifier tokenVerifier;
    private OrganizationsDataAccess repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsDataAccess repository) {
      this.repository = repository;
      return this;
    }

    public InviteMember build() {
      return new InviteMember(tokenVerifier, repository);
    }
  }
}
