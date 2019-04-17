package io.scalecube.organization.operation;

import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;

public class DeleteOrganizationApiKey
    extends ServiceOperation<DeleteOrganizationApiKeyRequest, GetOrganizationResponse> {

  private DeleteOrganizationApiKey(
      TokenVerifier tokenVerifier, OrganizationsRepository repository) {
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

    organization.removeApiKey(request.apiKeyName());
    context.repository().save(organization.id(), organization);

    Role role = getRole(context.profile().userId(), organization);
    return getOrganizationResponse(organization, apiKeyFilterBy(role));
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
    private OrganizationsRepository repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public DeleteOrganizationApiKey build() {
      return new DeleteOrganizationApiKey(tokenVerifier, repository);
    }
  }
}
