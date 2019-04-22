package io.scalecube.organization.operation;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.KeyStore;

public class DeleteOrganizationApiKey
    extends ServiceOperation<DeleteOrganizationApiKeyRequest, GetOrganizationResponse> {

  private final KeyStore keyStore;

  private DeleteOrganizationApiKey(
      TokenVerifier tokenVerifier, OrganizationsRepository repository, KeyStore keyStore) {
    super(tokenVerifier, repository);
    this.keyStore = keyStore;
  }

  @Override
  protected GetOrganizationResponse process(
      DeleteOrganizationApiKeyRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());

    if (organization.apiKeys() == null) {
      throw new IllegalStateException("organization.apiKeys is null");
    }

    checkSuperUserAccess(organization, context.profile());

    ApiKey apiKey =
        organization.apiKeys().stream()
            .filter(ak -> ak.name().equalsIgnoreCase(request.apiKeyName()))
            .findFirst()
            .orElse(null);

    if (apiKey != null && apiKey.keyId() != null) {
      keyStore.delete(apiKey.keyId());
    }

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
    private KeyStore keyStore;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public Builder keyStore(KeyStore keyStore) {
      this.keyStore = keyStore;
      return this;
    }

    public DeleteOrganizationApiKey build() {
      return new DeleteOrganizationApiKey(tokenVerifier, repository, keyStore);
    }
  }
}
