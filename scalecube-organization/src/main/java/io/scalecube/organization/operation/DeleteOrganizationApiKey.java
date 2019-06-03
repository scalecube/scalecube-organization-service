package io.scalecube.organization.operation;

import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.KeyStore;
import reactor.core.publisher.Mono;

public class DeleteOrganizationApiKey
    extends ServiceOperation<DeleteOrganizationApiKeyRequest, GetOrganizationResponse> {

  private final KeyStore keyStore;

  private DeleteOrganizationApiKey(
      TokenVerifier tokenVerifier, OrganizationsRepository repository, KeyStore keyStore) {
    super(tokenVerifier, repository);
    this.keyStore = keyStore;
  }

  @Override
  protected Mono<GetOrganizationResponse> process(
      DeleteOrganizationApiKeyRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(
            organization -> {
              if (organization.apiKeys() == null) {
                throw new IllegalStateException("organization.apiKeys is null");
              }

              checkSuperUserAccess(organization, context.profile());

              organization.apiKeys().stream()
                  .filter(apiKey -> apiKey.name().equalsIgnoreCase(request.apiKeyName()))
                  .findAny()
                  .ifPresent(
                      foundApiKey -> {
                        if (foundApiKey.keyId() != null) {
                          keyStore.delete(foundApiKey.keyId());
                        }
                      });

              organization.removeApiKey(request.apiKeyName());
            })
        .flatMap(organization -> context.repository().save(organization.id(), organization))
        .map(
            organization -> {
              Role role = getRole(context.profile().userId(), organization);
              return getOrganizationResponse(organization, apiKeyFilterBy(role));
            });
  }

  @Override
  protected Mono<Void> validate(
      DeleteOrganizationApiKeyRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () -> {
          requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
          requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
        });
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
