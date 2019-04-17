package io.scalecube.organization.operation;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.IdGenerator;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.KeyStore;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.UUID;

public final class CreateOrganization
    extends OrganizationInfoOperation<CreateOrganizationRequest, CreateOrganizationResponse> {

  private final KeyPairGenerator keyPairGenerator;
  private final KeyStore keyStore;

  private CreateOrganization(Builder builder) {
    super(builder.tokenVerifier, builder.repository);
    this.keyPairGenerator = builder.keyPairGenerator;
    this.keyStore = builder.keyStore;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  protected CreateOrganizationResponse process(
      CreateOrganizationRequest request, OperationServiceContext context) {
    String id = "ORG-" + IdGenerator.generateId();
    validate(
        new OrganizationInfo.Builder().id(id).email(request.email()).name(request.name()).build(),
        context);

    Organization organization = createOrganization(request, context, id);

    try {
      generateOrganizationKeyPair(organization);
    } catch (Exception ex) {
      // failed to persist organization secret rollback
      context.repository().deleteById(organization.id());
      throw ex;
    }

    return new CreateOrganizationResponse(
        OrganizationInfo.builder()
            .id(organization.id())
            .name(organization.name())
            .email(organization.email())
            .apiKeys(organization.apiKeys().toArray(new ApiKey[0])));
  }

  private Organization createOrganization(
      CreateOrganizationRequest request, OperationServiceContext context, String id) {
    Organization organization =
        new Organization(
            id,
            request.name(),
            request.email(),
            UUID.randomUUID().toString(),
            context.profile().userId());

    return context.repository().save(organization.id(), organization);
  }

  private void generateOrganizationKeyPair(Organization organization) {
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    keyStore.store(organization.keyId(), keyPair);
  }

  @Override
  protected Token getToken(CreateOrganizationRequest request) {
    return request.token();
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

    public CreateOrganization build() {
      return new CreateOrganization(this);
    }
  }
}
