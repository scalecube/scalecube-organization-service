package io.scalecube.organization.opearation;

import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;

public class UpdateOrganization
    extends OrganizationInfoOperation<UpdateOrganizationRequest, UpdateOrganizationResponse> {

  private UpdateOrganization(TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected UpdateOrganizationResponse process(
      UpdateOrganizationRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    checkSuperUserAccess(organization, context.profile());
    Organization orgUpdate =
        Organization.builder()
            .name(request.name())
            .email(request.email())
            .apiKey(organization.apiKeys())
            .copy(organization);

    context.repository().updateOrganizationDetails(context.profile(), organization, orgUpdate);
    return new UpdateOrganizationResponse(
        OrganizationInfo.builder()
            .id(orgUpdate.id())
            .name(orgUpdate.name())
            .apiKeys(orgUpdate.apiKeys())
            .email(orgUpdate.email()));
  }

  @Override
  protected void validate(UpdateOrganizationRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);

    validate(
        new OrganizationInfo.Builder()
            .id(request.organizationId())
            .email(request.email())
            .name(request.name())
            .build(),
        context);
  }

  @Override
  protected Token getToken(UpdateOrganizationRequest request) {
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

    public UpdateOrganization build() {
      return new UpdateOrganization(tokenVerifier, repository);
    }
  }
}
