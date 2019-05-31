package io.scalecube.organization.operation;

import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationMembersResponse;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import reactor.core.publisher.Mono;

public class GetOrganizationMembers
    extends ServiceOperation<GetOrganizationMembersRequest, GetOrganizationMembersResponse> {

  private GetOrganizationMembers(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected Mono<GetOrganizationMembersResponse> process(
      GetOrganizationMembersRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(organization -> checkSuperUserAccess(organization, context.profile()))
        .map(organization -> organization.members().toArray(new OrganizationMember[0]))
        .map(GetOrganizationMembersResponse::new);
  }

  @Override
  protected Mono<Void> validate(
      GetOrganizationMembersRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () ->
            requireNonNullOrEmpty(
                request.organizationId(), "organizationId is a required argument"));
  }

  @Override
  protected Token getToken(GetOrganizationMembersRequest request) {
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

    public GetOrganizationMembers build() {
      return new GetOrganizationMembers(tokenVerifier, repository);
    }
  }
}
