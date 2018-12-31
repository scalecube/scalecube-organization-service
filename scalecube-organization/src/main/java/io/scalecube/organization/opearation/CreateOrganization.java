package io.scalecube.organization.opearation;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.tokens.IdGenerator;
import io.scalecube.tokens.KeyStoreException;
import io.scalecube.tokens.TokenVerifier;
import io.scalecube.tokens.store.KeyStoreFactory;
import java.util.UUID;

public final class CreateOrganization extends OrganizationInfoOperation<CreateOrganizationRequest,
    CreateOrganizationResponse> {

  private CreateOrganization(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  protected CreateOrganizationResponse process(
      CreateOrganizationRequest request,
      OperationServiceContext context) throws Throwable {
    String id = IdGenerator.generateId();
    validate(new OrganizationInfo.Builder()
        .id(id)
        .email(context.profile().getEmail())
        .name(request.name())
        .ownerId(context.profile().getUserId())
        .build(), context);
    Organization organization = createOrganization(request, context, id);
    persistOrganizationSecret(context, organization);

    return new CreateOrganizationResponse(
        OrganizationInfo.builder()
            .id(organization.id())
            .name(organization.name())
            .apiKeys(organization.apiKeys())
            .email(organization.email())
            .ownerId(organization.ownerId()));
  }

  private Organization createOrganization(CreateOrganizationRequest request,
                                          OperationServiceContext context,
                                          String id) throws AccessPermissionException {
    return context.repository().createOrganization(context.profile(),
          Organization.builder()
              .id(id)
              .name(request.name())
              .ownerId(context.profile().getUserId())
              .email(context.profile().getEmail())
              .secretKeyId(UUID.randomUUID().toString())
              .secretKey(IdGenerator.generateId())
              .build());
  }

  /**
   * Persists the <code>organization</code> argument secret using the system configured
   * @{@link io.scalecube.tokens.store.KeyStore}.
   * Organization secrets are used to digitally sign API keys generated within the scope of an
   *   organization.
   * @param context Execution context
   * @param organization The organization which owns the secret to be persisted
   * @throws AccessPermissionException in case of an error
   * @throws EntityNotFoundException in case of an error
   * @throws KeyStoreException   in case of an error
   */
  private void persistOrganizationSecret(OperationServiceContext context,
                                         Organization organization)
      throws AccessPermissionException, EntityNotFoundException, KeyStoreException {
    try {
      KeyStoreFactory.get().store(organization.secretKeyId(), organization.secretKey());
    } catch (Throwable ex) {
      // failed to persist organization secret rollback
      context.repository().deleteOrganization(context.profile(), organization);
      throw ex;
    }
  }

  @Override
  protected Token getToken(CreateOrganizationRequest request) {
    return request.token();
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

    public CreateOrganization build() {
      return new CreateOrganization(tokenVerifier, repository);
    }
  }
}
