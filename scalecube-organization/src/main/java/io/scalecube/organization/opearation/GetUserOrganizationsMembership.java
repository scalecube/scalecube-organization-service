package io.scalecube.organization.opearation;

import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.tokens.TokenVerifier;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GetUserOrganizationsMembership extends ServiceOperation<GetMembershipRequest,
    GetMembershipResponse> {

  private GetUserOrganizationsMembership(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetMembershipResponse process(GetMembershipRequest request,
      OperationServiceContext context) throws Throwable {
    Collection<Organization> results = context.repository().getUserMembership(
        context.profile().getUserId());

    final List<OrganizationInfo> infoItems = results.stream().map(item ->
        OrganizationInfo.builder()
            .id(item.id())
            .name(item.name())
            .apiKeys(item.apiKeys())
            .email(item.email())
            .ownerId(item.ownerId())
            .build()).collect(Collectors.toList());

    return new GetMembershipResponse(
        infoItems.toArray(new OrganizationInfo[results.size()]));
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
