package io.scalecube.organization;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
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
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberResponse;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.ServiceOperationException;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationMemberRoleResponse;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.opearation.AddOrganizationApiKey;
import io.scalecube.organization.opearation.CreateOrganization;
import io.scalecube.organization.opearation.DeleteOrganization;
import io.scalecube.organization.opearation.DeleteOrganizationApiKey;
import io.scalecube.organization.opearation.GetOrganization;
import io.scalecube.organization.opearation.GetOrganizationMembers;
import io.scalecube.organization.opearation.GetUserOrganizationsMembership;
import io.scalecube.organization.opearation.InviteMember;
import io.scalecube.organization.opearation.KickoutMember;
import io.scalecube.organization.opearation.LeaveOrganization;
import io.scalecube.organization.opearation.UpdateOrganization;
import io.scalecube.organization.opearation.UpdateOrganizationMemberRole;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.tokens.TokenVerification;
import io.scalecube.tokens.TokenVerifier;
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

  private OrganizationServiceImpl(OrganizationsDataAccess repository, TokenVerifier tokenVerifier) {
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
    return Mono.create(
        result -> {
          logger.debug("createOrganization: enter, request: {}", request);

          try {
            CreateOrganizationResponse response =
                CreateOrganization.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);

            logger.debug("createOrganization: exit, return: {}", response);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("createOrganization: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<GetMembershipResponse> getUserOrganizationsMembership(GetMembershipRequest request) {
    return Mono.create(
        result -> {
          logger.debug("getUserOrganizationsMembership: enter, request: {}", request);

          try {
            GetMembershipResponse response =
                GetUserOrganizationsMembership.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug(
                "getUserOrganizationsMembership: exit, request: {}, return: {} memberships",
                request,
                response.organizations().length);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("getUserOrganizationsMembership: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<DeleteOrganizationResponse> deleteOrganization(DeleteOrganizationRequest request) {
    return Mono.create(
        result -> {
          logger.debug("deleteOrganization: enter, request: {}", request);

          try {
            DeleteOrganizationResponse response =
                DeleteOrganization.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug("deleteOrganization: exit, request: {}, response: {}", request, response);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("deleteOrganization: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<UpdateOrganizationResponse> updateOrganization(UpdateOrganizationRequest request) {
    return Mono.create(
        result -> {
          logger.debug("updateOrganization: enter, request: {}", request);

          try {
            UpdateOrganizationResponse response =
                UpdateOrganization.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug("updateOrganization: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("updateOrganization: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<GetOrganizationMembersResponse> getOrganizationMembers(
      GetOrganizationMembersRequest request) {
    return Mono.create(
        result -> {
          try {
            GetOrganizationMembersResponse response =
                GetOrganizationMembers.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug(
                "getOrganizationMembers: exit, org id: {}, return {} members",
                request.organizationId(),
                response.members().length);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("getOrganizationMembers: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<InviteOrganizationMemberResponse> inviteMember(
      InviteOrganizationMemberRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("inviteMember: enter, request: {}", request);
            InviteOrganizationMemberResponse response =
                InviteMember.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug("inviteMember: return response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("inviteMember: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<KickoutOrganizationMemberResponse> kickoutMember(
      KickoutOrganizationMemberRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("kickoutMember: enter, request: {}", request);
            KickoutOrganizationMemberResponse response =
                KickoutMember.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug("kickoutMember: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("kickoutMember: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<LeaveOrganizationResponse> leaveOrganization(LeaveOrganizationRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("leaveOrganization: enter, request: {}", request);
            LeaveOrganizationResponse response =
                LeaveOrganization.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug("leaveOrganization: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("leaveOrganization: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<GetOrganizationResponse> addOrganizationApiKey(AddOrganizationApiKeyRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("addOrganizationApiKey: enter, request: {}", request);
            GetOrganizationResponse response =
                AddOrganizationApiKey.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);

            logger.debug(
                "addOrganizationApiKey: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("addOrganizationApiKey: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<GetOrganizationResponse> deleteOrganizationApiKey(
      DeleteOrganizationApiKeyRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("deleteOrganizationApiKey: enter, request: {}", request);
            GetOrganizationResponse response =
                DeleteOrganizationApiKey.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug(
                "deleteOrganizationApiKey: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("deleteOrganizationApiKey: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("getOrganization: enter, request: {}", request);
            GetOrganizationResponse response =
                GetOrganization.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug("getOrganization: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("getOrganization: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
  }

  @Override
  public Mono<UpdateOrganizationMemberRoleResponse> updateOrganizationMemberRole(
      UpdateOrganizationMemberRoleRequest request) {
    return Mono.create(
        result -> {
          try {
            logger.debug("updateOrganizationMemberRole: enter, request: {}", request);
            UpdateOrganizationMemberRoleResponse response =
                UpdateOrganizationMemberRole.builder()
                    .tokenVerifier(tokenVerifier)
                    .repository(repository)
                    .build()
                    .execute(request);
            logger.debug(
                "updateOrganizationMemberRole: exit, response: {}, request: {}", response, request);
            result.success(response);
          } catch (ServiceOperationException ex) {
            logger.error("updateOrganizationMemberRole: ERROR: {}", ex);
            result.error(ex.getCause());
          }
        });
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
      OrganizationsDataAccess repository =
          OrganizationsDataAccessImpl.builder()
              .organizations(organizationRepository)
              .members(organizationMembershipRepository)
              .repositoryAdmin(organizationMembersRepositoryAdmin)
              .build();
      return new OrganizationServiceImpl(
          repository, tokenVerifier == null ? new TokenVerification() : tokenVerifier);
    }

    public Builder organizationRepository(Repository<Organization, String> organizationRepository) {
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
