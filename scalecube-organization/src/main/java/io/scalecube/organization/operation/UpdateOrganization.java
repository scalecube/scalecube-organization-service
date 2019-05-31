package io.scalecube.organization.operation;

import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import reactor.core.publisher.Mono;

public class UpdateOrganization
    extends OrganizationInfoOperation<UpdateOrganizationRequest, UpdateOrganizationResponse> {

  private UpdateOrganization(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected Mono<UpdateOrganizationResponse> process(
      UpdateOrganizationRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(
            organization -> {
              checkSuperUserAccess(organization, context.profile());

              organization.changeName(request.name());
              organization.changeEmail(request.email());
            })
        .flatMap(organization -> context.repository().save(organization.id(), organization))
        .map(
            organization -> {
              Role role = getRole(context.profile().userId(), organization);
              return new UpdateOrganizationResponse(
                  organizationInfo(organization, apiKeyFilterBy(role)));
            });
  }

  @Override
  protected Mono<Void> validate(
      UpdateOrganizationRequest request, OperationServiceContext context) {
    return validate(
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
