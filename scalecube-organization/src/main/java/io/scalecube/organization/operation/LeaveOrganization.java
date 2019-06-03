package io.scalecube.organization.operation;

import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import reactor.core.publisher.Mono;

public class LeaveOrganization
    extends ServiceOperation<LeaveOrganizationRequest, LeaveOrganizationResponse> {

  private LeaveOrganization(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected Mono<LeaveOrganizationResponse> process(
      LeaveOrganizationRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(
            organization -> {
              checkLastOwner(context.profile().userId(), organization);
              organization.removeMember(context.profile().userId());
            })
        .flatMap(organization -> context.repository().save(organization.id(), organization))
        .map(organization -> new LeaveOrganizationResponse());
  }

  @Override
  protected Mono<Void> validate(LeaveOrganizationRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () ->
            requireNonNullOrEmpty(
                request.organizationId(), "organizationId is a required argument"));
  }

  @Override
  protected Token getToken(LeaveOrganizationRequest request) {
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

    public LeaveOrganization build() {
      return new LeaveOrganization(tokenVerifier, repository);
    }
  }
}
