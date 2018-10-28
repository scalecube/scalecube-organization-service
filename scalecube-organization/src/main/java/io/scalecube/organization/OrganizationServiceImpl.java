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
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import io.scalecube.tokens.IdGenerator;
import io.scalecube.tokens.TokenVerification;
import io.scalecube.tokens.TokenVerifier;
import io.scalecube.tokens.store.ApiKeyBuilder;
import io.scalecube.tokens.store.KeyStoreFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Concrete implementation of {@link OrganizationService}.
 */
public class OrganizationServiceImpl implements OrganizationService {

  private final TokenVerifier tokenVerifier;
  private final OrganizationsDataAccess repository;
  private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);

  private OrganizationServiceImpl(OrganizationsDataAccess repository,
      TokenVerifier tokenVerifier) {
    this.repository = repository;
    this.tokenVerifier = tokenVerifier;
  }

  /**
   * Returns a builder instance of OrganizationServiceImpl.
   *
   * @return instance of OrganizationServiceImpl.Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Mono<CreateOrganizationResponse> createOrganization(CreateOrganizationRequest request) {
    return Mono.create(result -> {
      logger.debug("createOrganization: enter, request: {}", request);

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
                .secretKeyId(UUID.randomUUID().toString())
                .secretKey(secretKey)
                .build());

        KeyStoreFactory.get().store(organization.secretKeyId(), secretKey);

        CreateOrganizationResponse response = new CreateOrganizationResponse(
            OrganizationInfo.builder()
            .id(organization.id())
            .name(organization.name())
            .apiKeys(organization.apiKeys())
            .email(organization.email())
            .ownerId(organization.ownerId()));

        logger.debug("createOrganization: exit, return: {}", response);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("createOrganization: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetMembershipResponse> getUserOrganizationsMembership(
      GetMembershipRequest request) {
    return Mono.create(result -> {
      logger.debug("getUserOrganizationsMembership: enter, request: {}", request);
      Collection<Organization> results;

      try {
        validateRequest(request);

        Profile profile = verifyToken(request.token());
        results = repository.getUserMembership(profile.getUserId());

        if (results == null) {
          logger.warn("getUserOrganizationsMembership: request: {}, repository.getUserMembership"
              + "returned null collection", request);
          results = new ArrayList<>();
        }

        final List<OrganizationInfo> infoItems = results.stream().map(item ->
            OrganizationInfo.builder()
                .id(item.id())
                .name(item.name())
                .apiKeys(item.apiKeys())
                .email(item.email())
                .ownerId(item.ownerId())
                .build()).collect(Collectors.toList());

        logger.debug("getUserOrganizationsMembership: exit, request: {}, return: {} memberships",
            request, results.size());
        result.success(new GetMembershipResponse(
            infoItems.toArray(new OrganizationInfo[results.size()])));
      } catch (Throwable ex) {
        logger.error("getUserOrganizationsMembership: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<DeleteOrganizationResponse> deleteOrganization(DeleteOrganizationRequest request) {
    return Mono.create(result -> {
      logger.debug("deleteOrganization: enter, request: {}", request);

      try {
        validateRequest(request, request.organizationId(), request.token());

        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());

        repository.deleteOrganization(owner, organization);

        DeleteOrganizationResponse response = new DeleteOrganizationResponse(organization.id(),
            true);
        logger.debug("deleteOrganization: exit, request: {}, response: {}", request, response);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("deleteOrganization: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<UpdateOrganizationResponse> updateOrganization(UpdateOrganizationRequest request) {
    return Mono.create(result -> {
      logger.debug("updateOrganization: enter, request: {}", request);

      try {
        validateRequest(request);

        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());

        Organization orgUpdate = Organization.builder()
              .name(request.name())
              .email(request.email())
              .apiKey(organization.apiKeys())
              .copy(organization);

        repository.updateOrganizationDetails(owner, organization, orgUpdate);
        UpdateOrganizationResponse response = new UpdateOrganizationResponse(
            OrganizationInfo.builder()
            .id(orgUpdate.id())
            .name(orgUpdate.name())
            .apiKeys(orgUpdate.apiKeys())
            .email(orgUpdate.email())
            .ownerId(orgUpdate.ownerId())
            );
        logger.debug("updateOrganization: exit, response: {}, request: {}", response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("updateOrganization: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationMembersResponse> getOrganizationMembers(
      GetOrganizationMembersRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("getOrganizationMembers: enter, request: {}", request);

        validateRequest(request, request.organizationId(), request.token());

        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());
        Collection<OrganizationMember> organizationMembers = repository.getOrganizationMembers(
            profile,
            organization);

        if (organizationMembers == null) {
          logger.warn(
              "getOrganizationMembers: request: {}, repository.getOrganizationMembers "
                  + "returned null collection", request);
          organizationMembers = new ArrayList<>();
        }

        OrganizationMember[] members  = new OrganizationMember[organizationMembers.size()];
        logger.debug("getOrganizationMembers: exit, org id: {}, return {} members",
            request.organizationId(), organizationMembers.size());
        result.success(new GetOrganizationMembersResponse(organizationMembers.toArray(members)));
      } catch (Throwable ex) {
        logger.error("getOrganizationMembers: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<InviteOrganizationMemberResponse> inviteMember(
      InviteOrganizationMemberRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("inviteMember: enter, request: {}", request);

        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.userId(), "user id is required");

        Profile owner = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());

        repository.invite(owner, organization, request.userId());

        InviteOrganizationMemberResponse response = new InviteOrganizationMemberResponse();
        logger.debug("inviteMember: return response: {}, request: {}", response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("inviteMember: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<KickoutOrganizationMemberResponse> kickoutMember(KickoutOrganizationMemberRequest
      request) {
    return Mono.create(result -> {
      try {
        logger.debug("kickoutMember: enter, request: {}", request);
        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.userId(), "user id is required");

        Profile caller = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());
        boolean isOwner = Objects.equals(organization.ownerId(), caller.getUserId());

        if (!isOwner) {
          throw new AccessPermissionException("Not owner");
        }

        repository.kickout(caller, organization, request.userId());

        KickoutOrganizationMemberResponse response = new KickoutOrganizationMemberResponse();
        logger.debug("kickoutMember: exit, response: {}, request: {}", response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("kickoutMember: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<LeaveOrganizationResponse> leaveOrganization(LeaveOrganizationRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("leaveOrganization: enter, request: {}", request);

        validateRequest(request, request.organizationId(), request.token());

        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());

        repository.leave(organization, profile.getUserId());

        LeaveOrganizationResponse response = new LeaveOrganizationResponse();
        logger.debug("leaveOrganization: exit, response: {}, request: {}", response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("leaveOrganization: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationResponse> addOrganizationApiKey(
      AddOrganizationApiKeyRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("addOrganizationApiKey: enter, request: {}", request);

        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");

        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());
        checkIfUserIsAllowedToAddAnApiKey(profile, organization);


        ApiKey apiKey = ApiKeyBuilder.build(organization, request);
        int newLength = organization.apiKeys().length + 1;
        ApiKey[] apiKeys = Arrays.copyOf(organization.apiKeys(),newLength);

        apiKeys[organization.apiKeys().length] = apiKey;

        Organization clonedOrg = Organization.builder().apiKey(apiKeys).copy(organization);
        repository.updateOrganizationDetails(profile, organization, clonedOrg);

        GetOrganizationResponse response = getOrganizationResponse(clonedOrg);

        logger.debug("addOrganizationApiKey: exit, response: {}, request: {}", response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("addOrganizationApiKey: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  private void checkIfUserIsAllowedToAddAnApiKey(Profile profile, Organization organization)
      throws EntityNotFoundException, AccessPermissionException {
    logger.debug("checkIfUserIsAllowedToAddAnApiKey: enter, profile.id: {}, org: {}",
        profile.getUserId(),
        organization.id());
    boolean isOwner = Objects.equals(organization.ownerId(), profile.getUserId());
    if (!isOwner) {
      logger.debug("checkIfUserIsAllowedToAddAnApiKey: enter, profile.id: {}, org: {}. "
              + "User is not org owner",
          profile.getUserId(),
          organization.id());
      OrganizationMember member = repository.getOrganizationMembers(profile, organization)
          .stream()
          .filter(i -> Objects.equals(i.id(), profile.getUserId()))
          .findAny()
          .orElseThrow(() -> new AccessPermissionException(profile.getUserId()
              + " not a member in organization: " + organization.name()));
      boolean isMemberRole = Objects.equals(member.role(), Role.Member.toString());
      logger.error("checkIfUserIsAllowedToAddAnApiKey: enter, profile.id: {}, org: {}. "
          + "User is not org admin",
          profile.getUserId(),
          organization.id());
      if (isMemberRole) {
        throw new AccessPermissionException("Insufficient role permissions");
      }
    }
  }



  @Override
  public Mono<GetOrganizationResponse> deleteOrganizationApiKey(
      DeleteOrganizationApiKeyRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("deleteOrganizationApiKey: enter, request: {}", request);
        validateRequest(request, request.organizationId(), request.token());
        requireNonNullOrEmpty(request.apiKeyName(), "apiKeyName is a required argument");

        Profile profile = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());
        if (organization.apiKeys() == null) {
          throw new IllegalStateException("organization.apiKeys is null");
        }
        List<ApiKey> apiKeys = Arrays.asList(organization.apiKeys());
        Organization newOrg = Organization.builder().apiKey(apiKeys.stream()
            .filter(api -> !api.name().equals(request.apiKeyName())).toArray(
                ApiKey[]::new)).copy(organization);

        repository.updateOrganizationDetails(profile, organization, newOrg);

        GetOrganizationResponse response = getOrganizationResponse(newOrg);
        logger.debug("deleteOrganizationApiKey: exit, response: {}, request: {}",
            response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("deleteOrganizationApiKey: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("getOrganization0: enter, request: {}", request);
        validateRequest(request, request.organizationId(), request.token());

        Profile caller = verifyToken(request.token());
        Organization organization = getOrganization0(request.organizationId());
        boolean isOwner = Objects.equals(organization.ownerId(), caller.getUserId());

        if (!isOwner && !repository.isMember(caller.getUserId(), organization)) {
          logger.error("getOrganization0: caller: {}, not a member of organization: {}",
              caller.getUserId(), organization.id());
          throw new AccessPermissionException("Not a member of the organization");
        }

        GetOrganizationResponse response = getOrganizationResponse(organization);
        logger.debug("getOrganization0: exit, response: {}, request: {}", response, request);
        result.success(response);
      } catch (Throwable ex) {
        logger.error("getOrganization0: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  private GetOrganizationResponse getOrganizationResponse(Organization organization) {
    return new GetOrganizationResponse(OrganizationInfo.builder()
        .id(organization.id())
        .name(organization.name())
        .apiKeys(organization.apiKeys())
        .email(organization.email())
        .ownerId(organization.ownerId()));
  }

  private Organization getOrganization0(String id)
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
