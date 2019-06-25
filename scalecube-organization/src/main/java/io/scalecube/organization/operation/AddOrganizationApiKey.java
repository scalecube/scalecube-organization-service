package io.scalecube.organization.operation;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationServiceException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.ApiKeyBuilder;
import io.scalecube.organization.tokens.store.KeyStore;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.EnumSet;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AddOrganizationApiKey
    extends ServiceOperation<AddOrganizationApiKeyRequest, GetOrganizationResponse> {

  private final KeyPairGenerator keyPairGenerator;
  private final KeyStore keyStore;

  private AddOrganizationApiKey(Builder builder) {
    super(builder.tokenVerifier, builder.repository);
    this.keyPairGenerator = builder.keyPairGenerator;
    this.keyStore = builder.keyStore;
  }

  @Override
  protected Mono<GetOrganizationResponse> process(
      AddOrganizationApiKeyRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(
            organization -> {
              checkSuperUserAccess(organization, context.profile());

              Role callerRole = getRole(context.profile().userId(), organization);

              if (request.claims() != null) {
                String roleClaim = request.claims().get("role");

                if (roleClaim != null) {
                  if (EnumSet.allOf(Role.class).stream()
                      .noneMatch(role -> role.name().equals(roleClaim))) {
                    throw new OrganizationServiceException(
                        String.format("Role '%s' is invalid", roleClaim));
                  }

                  Role targetRole = Role.valueOf(roleClaim);

                  if (targetRole.isHigherThan(callerRole)) {
                    throw new AccessPermissionException(
                        String.format(
                            "user: '%s', name: '%s', role: '%s' "
                                + "cannot add api key with higher role '%s'",
                            context.profile().userId(),
                            context.profile().name(),
                            callerRole,
                            targetRole));
                  }
                }
              }

              String keyId = UUID.randomUUID().toString();

              KeyPair keyPair = generateKeyPair(keyId);
              ApiKey apiKey =
                  ApiKeyBuilder.build(keyPair.getPrivate(), organization.id(), keyId, request);
              organization.addApiKey(apiKey);
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
      AddOrganizationApiKeyRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () -> {
          requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
          requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
        })
        .then(Mono.defer(() -> getOrganization(request.organizationId())))
        .flatMapMany(organization -> Flux.fromIterable(organization.apiKeys()))
        .filter(apiKey -> apiKey.name().equals(request.apiKeyName()))
        .doOnNext(
            apiKey -> {
              throw new IllegalArgumentException(
                  "apiKey name:'" + apiKey.name() + "' already exists");
            })
        .then();
  }

  private KeyPair generateKeyPair(String keyId) {
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    keyStore.store(keyId, keyPair);
    return keyPair;
  }

  @Override
  protected Token getToken(AddOrganizationApiKeyRequest request) {
    return request.token();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private TokenVerifier tokenVerifier;
    private OrganizationsRepository repository;
    private KeyPairGenerator keyPairGenerator;
    private KeyStore keyStore;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public Builder keyPairGenerator(KeyPairGenerator keyPairGenerator) {
      this.keyPairGenerator = keyPairGenerator;
      return this;
    }

    public Builder keyStore(KeyStore keyStore) {
      this.keyStore = keyStore;
      return this;
    }

    public AddOrganizationApiKey build() {
      return new AddOrganizationApiKey(this);
    }
  }
}
