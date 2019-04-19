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
import java.util.UUID;

public final class CreateOrganization
    extends OrganizationInfoOperation<CreateOrganizationRequest, CreateOrganizationResponse> {

  private CreateOrganization(Builder builder) {
    super(builder.tokenVerifier, builder.repository);
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
            context.profile().userId());

    return context.repository().save(organization.id(), organization);
  }

  @Override
  protected Token getToken(CreateOrganizationRequest request) {
    return request.token();
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

    public CreateOrganization build() {
      return new CreateOrganization(this);
    }
  }
}
