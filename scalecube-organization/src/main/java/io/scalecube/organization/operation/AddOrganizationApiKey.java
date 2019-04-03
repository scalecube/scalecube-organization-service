package io.scalecube.organization.operation;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationServiceException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.ApiKeyBuilder;
import io.scalecube.organization.tokens.store.KeyStore;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

public class AddOrganizationApiKey
    extends ServiceOperation<AddOrganizationApiKeyRequest, GetOrganizationResponse> {

  private final KeyStore keyStore;

  private AddOrganizationApiKey(Builder builder) {
    super(builder.tokenVerifier, builder.repository);
    this.keyStore = builder.keyStore;
  }

  @Override
  protected GetOrganizationResponse process(
      AddOrganizationApiKeyRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());

    checkSuperUserAccess(organization, context.profile());

    Role callerRole = getRole(context.profile().getUserId(), organization);

    if (request.claims() != null) {
      String roleClaim = request.claims().get("role");

      if (roleClaim != null) {
        if (EnumSet.allOf(Role.class).stream().noneMatch(role -> role.name().equals(roleClaim))) {
          throw new OrganizationServiceException(String.format("Role '%s' is invalid", roleClaim));
        }

        Role targetRole = Role.valueOf(roleClaim);

        if (targetRole.isHigherThan(callerRole)) {
          throw new AccessPermissionException(
              String.format(
                  "user: '%s', name: '%s', role: '%s' cannot add api key with higher role '%s'",
                  context.profile().getUserId(),
                  context.profile().getName(),
                  callerRole,
                  targetRole));
        }
      }
    }

    ApiKey apiKey = ApiKeyBuilder.build(keyStore, organization, request);
    int newLength = organization.apiKeys().length + 1;
    ApiKey[] apiKeys = Arrays.copyOf(organization.apiKeys(), newLength);

    apiKeys[organization.apiKeys().length] = apiKey;

    Organization clonedOrg =
        Organization.builder().members(organization.members()).apiKeys(apiKeys).copy(organization);
    context.repository().updateOrganizationDetails(context.profile(), organization, clonedOrg);

    Role role = getRole(context.profile().getUserId(), organization);
    return getOrganizationResponse(clonedOrg, apiKeyFilterBy(role));
  }

  @Override
  protected void validate(AddOrganizationApiKeyRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);
    requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
    requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
    Organization organization = getOrganization(request.organizationId());
    boolean alreadyExists =
        Stream.of(organization.apiKeys())
            .anyMatch(existingKey -> existingKey.name().equals(request.apiKeyName()));
    if (alreadyExists) {
      throw new IllegalArgumentException(
          "apiKey name:'" + request.apiKeyName() + "' already exists");
    }
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
    private OrganizationsDataAccess repository;
    private KeyStore keyStore;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsDataAccess repository) {
      this.repository = repository;
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
