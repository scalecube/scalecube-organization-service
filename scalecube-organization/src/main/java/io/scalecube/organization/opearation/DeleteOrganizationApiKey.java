package io.scalecube.organization.opearation;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import io.scalecube.tokens.TokenVerifier;
import io.scalecube.tokens.store.ApiKeyBuilder;
import java.util.Arrays;
import java.util.Objects;

public class AddOrganizationApiKey extends ServiceOperation<AddOrganizationApiKeyRequest,
    GetOrganizationResponse> {

  private AddOrganizationApiKey(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  @Override
  protected GetOrganizationResponse process(AddOrganizationApiKeyRequest request,
      OperationServiceContext context) throws Throwable {
    Organization organization = getOrganization(request.organizationId());
    checkIfUserIsAllowedToAddAnApiKey(context, organization);


    ApiKey apiKey = ApiKeyBuilder.build(organization, request);
    int newLength = organization.apiKeys().length + 1;
    ApiKey[] apiKeys = Arrays.copyOf(organization.apiKeys(),newLength);

    apiKeys[organization.apiKeys().length] = apiKey;

    Organization clonedOrg = Organization.builder().apiKey(apiKeys).copy(organization);
    context.repository().updateOrganizationDetails(context.profile(), organization, clonedOrg);

    return getOrganizationResponse(clonedOrg);
  }

  private void checkIfUserIsAllowedToAddAnApiKey(OperationServiceContext context,
      Organization organization)
      throws EntityNotFoundException, AccessPermissionException {
    Profile profile = context.profile();
    boolean isOwner = Objects.equals(organization.ownerId(), profile.getUserId());
    if (!isOwner) {
      OrganizationMember member = context.repository().getOrganizationMembers(profile, organization)
          .stream()
          .filter(i -> Objects.equals(i.id(), profile.getUserId()))
          .findAny()
          .orElseThrow(() -> new AccessPermissionException(profile.getUserId()
              + " not a member in organization: " + organization.name()));
      boolean isMemberRole = Objects.equals(member.role(), Role.Member.toString());
      if (isMemberRole) {
        throw new AccessPermissionException("Insufficient role permissions");
      }
    }
  }

  private GetOrganizationResponse getOrganizationResponse(Organization organization) {
    return new GetOrganizationResponse(OrganizationInfo.builder()
        .id(organization.id())
        .name(organization.name())
        .apiKeys(organization.apiKeys())
        .email(organization.email())
        .ownerId(organization.ownerId()));
  }

  @Override
  protected void validate(AddOrganizationApiKeyRequest request) {
    super.validate(request);
    requireNonNullOrEmpty(request.organizationId(),
        "organizationId is a required argument");
    requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
  }

  @Override
  protected Token getToken(AddOrganizationApiKeyRequest request) {
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

    public AddOrganizationApiKey build() {
      return new AddOrganizationApiKey(tokenVerifier, repository);
    }
  }
}
