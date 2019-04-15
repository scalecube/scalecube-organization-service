package io.scalecube.organization.operation;

import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;

public class LeaveOrganization
    extends ServiceOperation<LeaveOrganizationRequest, LeaveOrganizationResponse> {

  private LeaveOrganization(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected LeaveOrganizationResponse process(
      LeaveOrganizationRequest request, OperationServiceContext context) {
    Organization organization = getOrganization(request.organizationId());
    checkLastOwner(context.profile().userId(), organization);

    organization.removeMember(context.profile().userId());
    context.repository().save(organization.id(), organization);

    return new LeaveOrganizationResponse();
  }

  @Override
  protected void validate(LeaveOrganizationRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
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
    private OrganizationsRepository repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public LeaveOrganization build() {
      return new LeaveOrganization(tokenVerifier, repository);
    }
  }
}
