package io.scalecube.organization.operation;

import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;

public class GetOrganization
    extends ServiceOperation<GetOrganizationRequest, GetOrganizationResponse> {

  private GetOrganization(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationResponse process(
      GetOrganizationRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    checkMemberAccess(organization, context.profile());
    Role role = getRole(context.profile().userId(), organization);
    return getOrganizationResponse(organization, apiKeyFilterBy(role));
  }

  @Override
  protected void validate(GetOrganizationRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
  }

  @Override
  protected Token getToken(GetOrganizationRequest request) {
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

    public GetOrganization build() {
      return new GetOrganization(tokenVerifier, repository);
    }
  }
}
