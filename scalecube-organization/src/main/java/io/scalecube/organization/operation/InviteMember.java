package io.scalecube.organization.operation;

import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.tokens.TokenVerifier;
import reactor.core.publisher.Mono;

public class InviteMember
    extends ServiceOperation<InviteOrganizationMemberRequest, InviteOrganizationMemberResponse> {

  private InviteMember(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected Mono<InviteOrganizationMemberResponse> process(
      InviteOrganizationMemberRequest request, OperationServiceContext context) {
    return getOrganization(request.organizationId())
        .doOnNext(
            organization -> {
              checkSuperUserAccess(organization, context.profile());
              Role invitedMemberRole = toRole(request.role());

              Role callerRole = getRole(context.profile().userId(), organization);

              if (invitedMemberRole.isHigherThan(callerRole)) {
                throw new AccessPermissionException(
                    String.format(
                        "user: '%s', name: '%s', role: '%s' cannot invite to a higher role: '%s'",
                        context.profile().userId(),
                        context.profile().name(),
                        callerRole,
                        invitedMemberRole.toString()));
              }

              organization.addMember(
                  new OrganizationMember(request.userId(), invitedMemberRole.name()));
            })
        .flatMap(organization -> context.repository().save(organization.id(), organization))
        .map(organization -> new InviteOrganizationMemberResponse());
  }

  @Override
  protected Mono<Void> validate(
      InviteOrganizationMemberRequest request, OperationServiceContext context) {
    return Mono.fromRunnable(
        () -> {
          requireNonNullOrEmpty(request.organizationId(), "organizationId is a required argument");
          requireNonNullOrEmpty(request.userId(), "user id is required");
        });
  }

  @Override
  protected Token getToken(InviteOrganizationMemberRequest request) {
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

    public InviteMember build() {
      return new InviteMember(tokenVerifier, repository);
    }
  }
}
