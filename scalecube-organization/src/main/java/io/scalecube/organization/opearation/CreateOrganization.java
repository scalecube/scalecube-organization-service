package io.scalecube.organization.opearation;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Token;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.tokens.IdGenerator;
import io.scalecube.tokens.TokenVerifier;
import io.scalecube.tokens.store.KeyStoreFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.UUID;

public final class CreateOrganization extends OrganizationInfoOperation<CreateOrganizationRequest,
    CreateOrganizationResponse> {

  private KeyPairGenerator keyPairGenerator;

  private CreateOrganization(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository, KeyPairGenerator keyPairGenerator) {
    super(tokenVerifier, repository);
    this.keyPairGenerator = keyPairGenerator;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  protected CreateOrganizationResponse process(
      CreateOrganizationRequest request,
      OperationServiceContext context) throws Throwable {
    String id = "ORG-" + IdGenerator.generateId();
    validate(new OrganizationInfo.Builder()
        .id(id)
        .email(request.email())
        .name(request.name())
        .build(), context);

    Organization organization = createOrganization(request, context, id);

    try {
      generateOrganizationKeyPair(organization);
    } catch (Exception ex) {
      // failed to persist organization secret rollback
      context.repository().deleteOrganization(context.profile(), organization);
      throw ex;
    }

    return new CreateOrganizationResponse(
        OrganizationInfo.builder()
            .id(organization.id())
            .name(organization.name())
            .apiKeys(organization.apiKeys())
            .email(organization.email()));
  }

  private Organization createOrganization(
      CreateOrganizationRequest request, OperationServiceContext context, String id)
      throws AccessPermissionException {
    return context.repository().createOrganization(context.profile(),
          Organization.builder()
              .id(id)
              .name(request.name())
              .ownerId(context.profile().getUserId())
              .email(request.email())
              .keyId(UUID.randomUUID().toString())
              .build());
  }

  private void generateOrganizationKeyPair(Organization organization) {
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    KeyStoreFactory.get().store(organization.keyId(), keyPair);
  }

  @Override
  protected Token getToken(CreateOrganizationRequest request) {
    return request.token();
  }

  public static class Builder {
    private TokenVerifier tokenVerifier;
    private OrganizationsDataAccess repository;
    private KeyPairGenerator keyPairGenerator;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsDataAccess repository) {
      this.repository = repository;
      return this;
    }

    public Builder keyPairGenerator(KeyPairGenerator keyPairGenerator) {
      this.keyPairGenerator = keyPairGenerator;
      return this;
    }

    public CreateOrganization build() {
      return new CreateOrganization(tokenVerifier, repository, keyPairGenerator);
    }
  }
}
