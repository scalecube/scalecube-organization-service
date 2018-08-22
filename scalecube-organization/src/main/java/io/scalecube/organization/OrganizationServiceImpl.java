package io.scalecube.organization;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationMembersResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberResponse;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationNotFound;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import io.scalecube.tokens.IdGenerator;
import io.scalecube.tokens.JwtApiKey;
import io.scalecube.tokens.TokenVerification;
import io.scalecube.tokens.TokenVerifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

public class OrganizationServiceImpl implements OrganizationService {
  private final TokenVerifier tokenVerifier;
  private final OrganizationsDataAccess repository;

  private OrganizationServiceImpl(OrganizationsDataAccess repository,
      TokenVerifier tokenVerifier) {
    this.repository = repository;
    this.tokenVerifier = tokenVerifier;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Mono<CreateOrganizationResponse> createOrganization(CreateOrganizationRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request);
        Profile profile = verifyToken(request.token());
        String secretKey = IdGenerator.generateId();
        Organization organization = repository.createOrganization(profile,
            Organization.builder()
                .id(IdGenerator.generateId())
                .name(request.name())
                .ownerId(profile.getUserId())
                .email(request.email())
                .secretKey(secretKey)
                .build());

        result.success(new CreateOrganizationResponse(organization.id(),
            organization.name(),
            organization.apiKeys(),
            organization.email(),
            organization.ownerId()));
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetMembershipResponse> getUserOrganizationsMembership(
      GetMembershipRequest request) {
    return Mono.create(result -> {
      Collection<Organization> results;
      try {
        validateRequest(request);
        Profile profile = verifyToken(request.token());
        results = repository.getUserMembership(profile.getUserId());
        final List<OrganizationInfo> infoItems = results.stream().map(item ->
            new OrganizationInfo(item.id(),
                item.name(),
                item.apiKeys(),
                item.email(),
                item.ownerId())).collect(Collectors.toList());
        result.success(new GetMembershipResponse(
            infoItems.toArray(new OrganizationInfo[results.size()])));
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<DeleteOrganizationResponse> deleteOrganization(DeleteOrganizationRequest request) {

    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());
        repository.deleteOrganization(owner, organization);
        result.success(new DeleteOrganizationResponse(organization.id(), true));
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<UpdateOrganizationResponse> updateOrganization(UpdateOrganizationRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request);
        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());

        Organization org2 = Organization.builder()
              .name(request.name())
              .email(request.email())
              .copy(organization);

        repository.updateOrganizationDetails(owner, organization, org2);
        result.success(new UpdateOrganizationResponse(org2.id(), org2.name(),
            org2.apiKeys(),
            org2.email(), org2.ownerId()));
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationMembersResponse> getOrganizationMembers(
      GetOrganizationMembersRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        verifyToken(request.token());
        Collection<OrganizationMember> organizationMembers = repository.getOrganizationMembers(
            request.organizationId());
        result.success(
            new GetOrganizationMembersResponse(
                organizationMembers
                    .toArray(
                        new OrganizationMember[0]))
        );
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<InviteOrganizationMemberResponse> inviteMember(
      InviteOrganizationMemberRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.userId(), "user id is required");
        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());
        repository.invite(owner, organization, request.userId());
        result.success(new InviteOrganizationMemberResponse());
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<KickoutOrganizationMemberResponse> kickoutMember(KickoutOrganizationMemberRequest
      request) {
    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.userId(), "user id is required");
        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());
        repository.kickout(owner, organization, request.userId());
        result.success(new KickoutOrganizationMemberResponse());
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<LeaveOrganizationResponse> leaveOrganization(LeaveOrganizationRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());
        repository.leave(organization, profile.getUserId());
        result.success(new LeaveOrganizationResponse());
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationResponse> addOrganizationApiKey(
      AddOrganizationApiKeyRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());

        ApiKey apiKey = JwtApiKey.builder().issuer("scalecube.io")
            .subject(organization.id())
            .name(request.apiKeyName())
            .claims(request.claims())
            .id(organization.id())
            .build(organization.secretKey());
        ApiKey[] apiKeys = Arrays.copyOf(organization.apiKeys(),
            organization.apiKeys().length + 1);
        apiKeys[organization.apiKeys().length] = apiKey;
        Organization clonedOrg = Organization.builder().apiKey(apiKeys).copy(organization);
        repository.updateOrganizationDetails(profile, organization, clonedOrg);
        result.success(new GetOrganizationResponse(clonedOrg.id(), clonedOrg.name(),
            clonedOrg.apiKeys(),
            clonedOrg.email(),
            clonedOrg.ownerId()));
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationResponse> deleteOrganizationApiKey(
      DeleteOrganizationApiKeyRequest request) {

    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");
        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());
        List<ApiKey> apiKeys = Arrays.asList(organization.apiKeys());
        Organization newOrg = Organization.builder().apiKey(apiKeys.stream()
            .filter(api -> !api.name().equals(request.apiKeyName())).toArray(
                ApiKey[]::new)).copy(organization);
        repository.updateOrganizationDetails(profile, organization, newOrg);
        result.success(new GetOrganizationResponse(newOrg.id(),
            newOrg.name(),
            newOrg.apiKeys(),
            newOrg.email(),
            newOrg.ownerId()));

      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
    return Mono.create(result -> {
      try {
        validateRequest(request, request.organizationId(), request.token());
        verifyToken(request.token());
        Organization organization = getOrganization(request.organizationId());
        result.success(
            new GetOrganizationResponse(organization.id(), organization.name(),
                organization.apiKeys(),
                organization.email(),
                organization.ownerId()));
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }

  private Organization getOrganization(String id)
      throws EntityNotFoundException, OrganizationNotFound {
    Organization organization = repository.getOrganization(id);

    if (organization == null) {
      throw new OrganizationNotFound(id);
    }

    return organization;
  }

  private void validateRequest(UpdateOrganizationRequest request) {
    validateRequest(request, request.organizationId(), request.token());
    requireNonNullOrEmpty(request.name(), "Organization name is required");
    requireNonNullOrEmpty(request.email(), "Organization email is required");
  }

  private void validateRequest(CreateOrganizationRequest request) {
    Objects.requireNonNull(request, "request is a required argument");
    Objects.requireNonNull(request.token(), "token is a required argument");
    requireNonNullOrEmpty(request.token().token(), "token is a required argument");
    requireNonNullOrEmpty(request.email(), "email is a required argument");
    requireNonNullOrEmpty(request.name(), "name is a required argument");
  }

  private void validateRequest(GetMembershipRequest request) {
    Objects.requireNonNull(request, "request is a required argument");
    Objects.requireNonNull(request.token(), "token is a required argument");
    requireNonNullOrEmpty(request.token().token(), "token is a required argument");
  }

  private void validateRequest(Object request, String orgId, Object token) {
    requireNonNullOrEmpty(request, "request is a required argument");
    requireNonNullOrEmpty(orgId, "organizationId is a required argument");
    requireNonNullOrEmpty(token, "token is a required argument");
    if (token instanceof Token) {
      requireNonNullOrEmpty(((Token)token).token(), "token is a required argument");
    }
  }

  private Profile verifyToken(Token token) throws Throwable {
    Profile owner = tokenVerifier.verify(token);
    if (owner == null) {
      throw new InvalidAuthenticationToken();
    }
    return owner;
  }

  private static void requireNonNullOrEmpty(Object  object, String message) {
    Objects.requireNonNull(object, message);

    if (object.toString().length() == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  public static class Builder {

    private Repository<Organization, String> organizationRepository;
    private TokenVerifier tokenVerifier;
    private UserOrganizationMembershipRepository organizationMembershipRepository;
    private OrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin;

    /**
     * Construct an OrganizationService object with the provided parameters.
     *
     * @return an instance of OrganizationService.
     */
    public OrganizationService build() {
      OrganizationsDataAccess repository = OrganizationsDataAccessImpl
          .builder()
          .organizations(organizationRepository)
          .members(organizationMembershipRepository)
          .repositoryAdmin(organizationMembersRepositoryAdmin)
          .build();
      return new OrganizationServiceImpl(repository, tokenVerifier == null
          ? new TokenVerification()
          : tokenVerifier);
    }

    public Builder organizationRepository(
        Repository<Organization, String> organizationRepository) {
      this.organizationRepository = organizationRepository;
      return this;
    }

    public Builder organizationMembershipRepository(
        UserOrganizationMembershipRepository organizationMembershipRepository) {
      this.organizationMembershipRepository = organizationMembershipRepository;
      return this;
    }

    public Builder organizationMembershipRepositoryAdmin(
        OrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin) {
      this.organizationMembersRepositoryAdmin = organizationMembersRepositoryAdmin;
      return this;
    }

    Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }
  }
}
