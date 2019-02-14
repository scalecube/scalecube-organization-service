package io.scalecube.organization.opearation;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Token;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;
import java.util.Arrays;
import java.util.List;

public class DeleteOrganizationApiKey
    extends ServiceOperation<DeleteOrganizationApiKeyRequest, GetOrganizationResponse> {

  private DeleteOrganizationApiKey(
      TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationResponse process(
      DeleteOrganizationApiKeyRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());

    if (organization.apiKeys() == null) {
      throw new IllegalStateException("organization.apiKeys is null");
    }

    checkSuperUserAccess(organization, context.profile());

    List<ApiKey> apiKeys = Arrays.asList(organization.apiKeys());
    Organization newOrg =
        Organization.builder()
            .apiKey(
                apiKeys
                    .stream()
                    .filter(api -> !api.name().equals(request.apiKeyName()))
                    .toArray(ApiKey[]::new))
            .copy(organization);

    context.repository().updateOrganizationDetails(context.profile(), organization, newOrg);

    return getOrganizationResponse(newOrg);
  }

  @Override
  protected void validate(DeleteOrganizationApiKeyRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
    requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
  }

  @Override
  protected Token getToken(DeleteOrganizationApiKeyRequest request) {
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

    public DeleteOrganizationApiKey build() {
      return new DeleteOrganizationApiKey(tokenVerifier, repository);
    }
  }
}
