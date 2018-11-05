package io.scalecube.organization.opearation;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationMembersResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;

import java.util.Collection;

public class GetOrganizationMembers extends ServiceOperation<GetOrganizationMembersRequest,
    GetOrganizationMembersResponse> {

  private GetOrganizationMembers(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationMembersResponse process(GetOrganizationMembersRequest request,
      OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    Collection<OrganizationMember> organizationMembers = context.repository()
        .getOrganizationMembers(context.profile(), organization);
    OrganizationMember[] members  = new OrganizationMember[organizationMembers.size()];
    return new GetOrganizationMembersResponse(organizationMembers.toArray(members));
  }

  @Override
  protected void validate(GetOrganizationMembersRequest request) {
    super.validate(request);
    requireNonNullOrEmpty(request.organizationId(),
        "organizationId is a required argument");
  }

  @Override
  protected Token getToken(GetOrganizationMembersRequest request) {
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

    public GetOrganizationMembers build() {
      return new GetOrganizationMembers(tokenVerifier, repository);
    }
  }
}