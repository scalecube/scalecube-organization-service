package io.scalecube.organization.opearation;

import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;

public class InviteMember extends ServiceOperation<InviteOrganizationMemberRequest,
    InviteOrganizationMemberResponse> {

  private InviteMember(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected InviteOrganizationMemberResponse process(InviteOrganizationMemberRequest request,
      OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    context.repository().invite(context.profile(), organization, request.userId());
    return new InviteOrganizationMemberResponse();
  }

  @Override
  protected void validate(InviteOrganizationMemberRequest request) {
    super.validate(request);
    requireNonNullOrEmpty(request.organizationId(),
        "organizationId is a required argument");
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
