package io.scalecube.organization.operation;

import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;

public class UpdateOrganization
    extends OrganizationInfoOperation<UpdateOrganizationRequest, UpdateOrganizationResponse> {

  private UpdateOrganization(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected UpdateOrganizationResponse process(
      UpdateOrganizationRequest request, OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());

    checkSuperUserAccess(organization, context.profile());

    organization.changeName(request.name());
    organization.changeEmail(request.email());

    context.repository().save(organization.id(), organization);

    Role role = getRole(context.profile().getUserId(), organization);
    return new UpdateOrganizationResponse(organizationInfo(organization, apiKeyFilterBy(role)));
  }

  @Override
  protected void validate(UpdateOrganizationRequest request, OperationServiceContext context)
      throws Throwable {
    super.validate(request, context);

    validate(
        new OrganizationInfo.Builder()
            .id(request.organizationId())
            .email(request.email())
            .name(request.name())
            .build(),
        context);
  }

  @Override
  protected Token getToken(UpdateOrganizationRequest request) {
    return request.token();
  }

  public static Builder builder() {
    return new Builder();
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

    public UpdateOrganization build() {
      return new UpdateOrganization(tokenVerifier, repository);
    }
  }
}
