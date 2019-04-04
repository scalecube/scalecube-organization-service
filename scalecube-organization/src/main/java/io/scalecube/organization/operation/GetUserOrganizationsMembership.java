package io.scalecube.organization.operation;

import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import java.util.stream.StreamSupport;

public class GetUserOrganizationsMembership
    extends ServiceOperation<GetMembershipRequest, GetMembershipResponse> {

  private GetUserOrganizationsMembership(
      TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetMembershipResponse process(
      GetMembershipRequest request, OperationServiceContext context) {
    return new GetMembershipResponse(
        StreamSupport.stream(context.repository().findAll().spliterator(), false)
            .filter(organization -> organization.isMember(context.profile().getUserId()))
            .map(
                organization -> {
                  Role role = getRole(context.profile().getUserId(), organization);
                  return organizationInfo(organization, apiKeyFilterBy(role)).build();
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
    private OrganizationsRepository repository;

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder repository(OrganizationsRepository repository) {
      this.repository = repository;
      return this;
    }

    public GetUserOrganizationsMembership build() {
      return new GetUserOrganizationsMembership(tokenVerifier, repository);
    }
  }
}
