package io.scalecube.organization.opearation;

import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.tokens.TokenVerifier;

import java.util.Objects;

public class GetOrganization extends ServiceOperation<GetOrganizationRequest,
    GetOrganizationResponse> {

  private GetOrganization(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationResponse process(GetOrganizationRequest request,
      OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    boolean isOwner = Objects.equals(organization.ownerId(), context.profile().getUserId());

    if (!isOwner && !context.repository().isMember(context.profile().getUserId(), organization)) {
      throw new AccessPermissionException("Not a member of the organization");
    }

    return getOrganizationResponse(organization);
  }

  @Override
  protected void validate(GetOrganizationRequest request) {
    super.validate(request);
    requireNonNullOrEmpty(request.organizationId(),
        "organizationId is a required argument");
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
    private OrganizationsDataAccess repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsDataAccess repository) {
      this.repository = repository;
      return this;
    }

    public GetOrganization build() {
      return new GetOrganization(tokenVerifier, repository);
    }
  }
}
