package io.scalecube.organization.opearation;

import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;

public class LeaveOrganization extends ServiceOperation<LeaveOrganizationRequest,
    LeaveOrganizationResponse> {

  private LeaveOrganization(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected LeaveOrganizationResponse process(LeaveOrganizationRequest request,
      OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    context.repository().leave(organization, context.profile().getUserId());
    return new LeaveOrganizationResponse();
  }

  @Override
  protected void validate(LeaveOrganizationRequest request) {
    super.validate(request);
    requireNonNullOrEmpty(request.organizationId(),
        "organizationId is a required argument");
  }

  @Override
  protected Token getToken(LeaveOrganizationRequest request) {
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

    public LeaveOrganization build() {
      return new LeaveOrganization(tokenVerifier, repository);
    }
  }
}
