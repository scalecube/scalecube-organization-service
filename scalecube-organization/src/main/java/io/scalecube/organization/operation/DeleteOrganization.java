package io.scalecube.organization.operation;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.KeyStore;
import reactor.core.publisher.Mono;

public class DeleteOrganization
    extends ServiceOperation<DeleteOrganizationRequest, DeleteOrganizationResponse> {
  private final KeyStore keyStore;

  private DeleteOrganization(
      TokenVerifier tokenVerifier, OrganizationsRepository repository, KeyStore keyStore) {
    super(tokenVerifier, repository);
    this.keyStore = keyStore;
  }

  @Override
  protected Mono<DeleteOrganizationResponse> process(
      DeleteOrganizationRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(organization -> checkOwnerAccess(organization, context.profile()))
        .flatMap(
            organization ->
                context
                    .repository()
                    .deleteById(organization.id())
                    .doOnSuccess(
                        ignore ->
                            organization.apiKeys().stream()
                                .map(ApiKey::keyId)
                                .filter(keyId -> keyId != null && !keyId.isEmpty())
                                .forEach(keyStore::delete))
                    .then(Mono.just(new DeleteOrganizationResponse(organization.id(), true))));
  }

  @Override
  protected Mono<Void> validate(
      DeleteOrganizationRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () ->
            requireNonNullOrEmpty(
                request.organizationId(), "organizationId is a required argument"));
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

    public DeleteOrganization build() {
      return new DeleteOrganization(tokenVerifier, repository, keyStore);
    }

    public Builder keyStore(KeyStore keyStore) {
      this.keyStore = keyStore;
      return this;
    }
  }
}
