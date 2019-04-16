package io.scalecube.organization.operation;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationMembersResponse;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;

public class GetOrganizationMembers
    extends ServiceOperation<GetOrganizationMembersRequest, GetOrganizationMembersResponse> {

  private GetOrganizationMembers(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationMembersResponse process(
      GetOrganizationMembersRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());

    checkSuperUserAccess(organization, context.profile());

    return new GetOrganizationMembersResponse(
        organization.members().toArray(new OrganizationMember[0]));
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
    private OrganizationsRepository repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public GetOrganizationMembers build() {
      return new GetOrganizationMembers(tokenVerifier, repository);
    }
  }
}
