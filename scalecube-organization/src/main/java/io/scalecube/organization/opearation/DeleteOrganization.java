package io.scalecube.organization.opearation;

import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;

public class DeleteOrganization extends ServiceOperation<DeleteOrganizationRequest,
    DeleteOrganizationResponse> {

  private DeleteOrganization(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected DeleteOrganizationResponse process(DeleteOrganizationRequest request,
      OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    context.repository().deleteOrganization(context.profile(), organization);
    return new DeleteOrganizationResponse(organization.id(), true);
  }

  @Override
  protected void validate(DeleteOrganizationRequest request) {
    super.validate(request);
    requireNonNullOrEmpty(request.organizationId(),
        "organizationId is a required argument");
  }

  @Override
  protected Token getToken(DeleteOrganizationRequest request) {
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

    public DeleteOrganization build() {
      return new DeleteOrganization(tokenVerifier, repository);
    }
  }
}
