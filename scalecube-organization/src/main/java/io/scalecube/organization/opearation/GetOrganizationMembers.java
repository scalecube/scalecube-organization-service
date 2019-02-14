package io.scalecube.organization.opearation;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationMembersResponse;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;
import java.util.Collection;

public class GetOrganizationMembers
    extends ServiceOperation<GetOrganizationMembersRequest, GetOrganizationMembersResponse> {

  private GetOrganizationMembers(TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationMembersResponse process(
      GetOrganizationMembersRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());

    checkMemberAccess(organization, context.profile());
    Collection<OrganizationMember> organizationMembers =
        context.repository().getOrganizationMembers(organization);
    OrganizationMember[] members = new OrganizationMember[organizationMembers.size()];

    return new GetOrganizationMembersResponse(organizationMembers.toArray(members));
  }

  @Override
  protected void validate(GetOrganizationMembersRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
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
