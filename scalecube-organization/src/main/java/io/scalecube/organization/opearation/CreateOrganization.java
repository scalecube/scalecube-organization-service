package io.scalecube.organization.opearation;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.IdGenerator;
import io.scalecube.tokens.TokenVerifier;
import io.scalecube.tokens.store.KeyStoreFactory;

import java.util.Objects;
import java.util.UUID;

public final class CreateOrganization extends ServiceOperation<CreateOrganizationRequest,
    CreateOrganizationResponse> {

  private CreateOrganization(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  protected CreateOrganizationResponse process(CreateOrganizationRequest request,
      OperationServiceContext context) throws Throwable {
    String secretKey = IdGenerator.generateId();
    Organization organization = context.repository().createOrganization(context.profile(),
        Organization.builder()
            .id(IdGenerator.generateId())
            .name(request.name())
            .ownerId(context.profile().getUserId())
            .email(request.email())
            .secretKeyId(UUID.randomUUID().toString())
            .secretKey(secretKey)
            .build());

    KeyStoreFactory.get().store(organization.secretKeyId(), secretKey);

    return new CreateOrganizationResponse(
        OrganizationInfo.builder()
            .id(organization.id())
            .name(organization.name())
            .apiKeys(organization.apiKeys())
            .email(organization.email())
            .ownerId(organization.ownerId()));
  }

  @Override
  protected void validate(CreateOrganizationRequest request) {
    requireNonNullOrEmpty(request.email(), "email is a required argument");
    requireNonNullOrEmpty(request.name(), "name is a required argument");
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
