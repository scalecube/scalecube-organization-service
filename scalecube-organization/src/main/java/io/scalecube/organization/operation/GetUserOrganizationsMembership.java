package io.scalecube.organization.operation;

import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.tokens.TokenVerifier;

public class GetUserOrganizationsMembership
    extends ServiceOperation<GetMembershipRequest, GetMembershipResponse> {

  private GetUserOrganizationsMembership(
      TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetMembershipResponse process(
      GetMembershipRequest request, OperationServiceContext context) {
    return new GetMembershipResponse(
        context
            .repository()
            .getUserMembership(context.profile().getUserId())
            .stream()
            .map(
                item -> {
                  Role role = getRole(context.profile().getUserId(), item);
                  return organizationInfo(item, apiKeyFilterBy(role)).build();
                })
            .toArray(OrganizationInfo[]::new));
  }

  @Override
  protected Token getToken(GetMembershipRequest request) {
    return request.token();
  }

  public static Builder builder() {
    return new Builder();
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

    public GetUserOrganizationsMembership build() {
      return new GetUserOrganizationsMembership(tokenVerifier, repository);
    }
  }
}
