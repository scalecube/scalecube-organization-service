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
import io.scalecube.account.api.InvalidRequestException;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberResponse;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;
import io.scalecube.account.api.MissingOrganizationException;
import io.scalecube.account.api.NoSuchOrganizationFound;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.security.Profile;
import io.scalecube.tokens.IdGenerator;
import io.scalecube.tokens.JwtApiKey;
import io.scalecube.tokens.TokenVerification;
import io.scalecube.tokens.TokenVerifier;

import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

  private static void requireNonNull(Object request, String orgId, Object token) {
    Objects.requireNonNull(request, "request is a required argument");
    Objects.requireNonNull(orgId,
        "organizationId is a required argument");
    Objects.requireNonNull(token, "token is a required argument");
  }

  @Override
  public Mono<CreateOrganizationResponse> createOrganization(CreateOrganizationRequest request) {
    Objects.requireNonNull(request, "request is a required argument");

    return Mono.create(result -> {
      try {
        final Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          final String secretKey = IdGenerator.generateId();
          final Organization newOrg = repository.createOrganization(profile,
              Organization.builder()
                  .id(IdGenerator.generateId())
                  .name(request.name())
                  .ownerId(profile.getUserId())
                  .email(request.email())
                  .secretKey(secretKey)
                  .build());

          result.success(new CreateOrganizationResponse(newOrg.id(),
              newOrg.name(),
              newOrg.apiKeys(),
              newOrg.email(),
              newOrg.ownerId()));

        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetMembershipResponse> getUserOrganizationsMembership(
      GetMembershipRequest request) {
    Objects.requireNonNull(request, "request is a required argument");
    Objects.requireNonNull(request.token(), "token is a required argument");

    return Mono.create(result -> {
      Collection<Organization> results;
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          results = repository.getUserMembership(profile.getUserId());
          final List<OrganizationInfo> infoItems = results.stream().map(item ->
              new OrganizationInfo(item.id(),
                  item.name(),
                  item.apiKeys(),
                  item.email(),
                  item.ownerId())).collect(Collectors.toList());
          result.success(new GetMembershipResponse(
              infoItems.toArray(new OrganizationInfo[results.size()])));
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  
  @Override
  public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
    requireNonNull(request, request.organizationId(), request.token());

    return Mono.create(result -> {
      Organization organization;
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          organization = repository.getOrganization(request.organizationId());
          if (organization != null) {
            result.success(
                new GetOrganizationResponse(organization.id(), organization.name(),
                    organization.apiKeys(),
                    organization.email(),
                    organization.ownerId()));
          } else {
            result.error(new MissingOrganizationException(request.organizationId()));
          }
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }

    });
  }

  @Override
  public Mono<DeleteOrganizationResponse> deleteOrganization(DeleteOrganizationRequest request) {
    requireNonNull(request, request.organizationId(), request.token());

    return Mono.create(result -> {
      try {
        final Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          final Organization org = repository.getOrganization(request.organizationId());
          if (org != null) {
            repository.deleteOrganization(profile, org);
            result.success(new DeleteOrganizationResponse(org.id(), true));
          } else {
            result.error(new NoSuchOrganizationFound(request.organizationId()));
          }
        } else {
          result.error(new InvalidAuthenticationToken());
        }

      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<UpdateOrganizationResponse> updateOrganization(UpdateOrganizationRequest request) {
    requireNonNull(request, request.organizationId(), request.token());
    return Mono.create(result -> {
      try {
        final Profile owner = tokenVerifier.verify(request.token());
        if (owner != null) {
          final Organization org = repository.getOrganization(request.organizationId());
          if (org != null) {

            Organization org2 = Organization.builder()
                .name(request.name())
                .email(request.email())
                .copy(org);

            repository.updateOrganizationDetails(owner, org, org2);
            result.success(new UpdateOrganizationResponse(org2.id(), org2.name(),
                org2.apiKeys(),
                org2.email(), org2.ownerId()));
          } else {
            result.error(new NoSuchOrganizationFound(request.organizationId()));
          }
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationMembersResponse> getOrganizationMembers(
      GetOrganizationMembersRequest request) {
    requireNonNull(request, request.organizationId(), request.token());
    return Mono.create(result -> {
      Collection<OrganizationMember> organizationMembers;
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          organizationMembers = repository.getOrganizationMembers(
              request.organizationId());
          result.success(
              new GetOrganizationMembersResponse(
                  organizationMembers
                      .toArray(
                          new OrganizationMember[0]))
          );
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<InviteOrganizationMemberResponse> inviteMember(
      InviteOrganizationMemberRequest request) {
    Objects.requireNonNull(request, "request is a required argument");
    Objects.requireNonNull(request.token(), "token is a required argument");

    return Mono.create(result -> {
      try {
        Profile owner = tokenVerifier.verify(request.token());
        if (owner != null) {
          Organization organization = repository.getOrganization(request.organizationId());
          if (organization != null && request.userId() != null) {
            repository.invite(owner, organization, request.userId());
            result.success(new InviteOrganizationMemberResponse());
          } else {
            result.error(new InvalidRequestException(
                "Cannot complete request, target-organization or target-profile was "
                    + "not found."));
          }
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<KickoutOrganizationMemberResponse> kickoutMember(
      KickoutOrganizationMemberRequest request) {

    requireNonNull(request, request.organizationId(), request.token());
    Objects.requireNonNull(request.userId(), "userId is a required argument");

    return Mono.create(result -> {
      try {
        Profile owner = tokenVerifier.verify(request.token());
        if (owner != null) {
          Organization organization = repository.getOrganization(
              request.organizationId());
          if (organization != null && request.userId() != null) {
            repository.kickout(owner, organization, request.userId());
            result.success(new KickoutOrganizationMemberResponse());
          } else {
            result.error(new InvalidRequestException(
                "Cannot complete request, target-organization or target-profile was "
                    + "not found."));
          }
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<LeaveOrganizationResponse> leaveOrganization(LeaveOrganizationRequest request) {
    requireNonNull(request, request.organizationId(), request.token());
    return Mono.create(result -> {
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          Organization organization = repository.getOrganization(
              request.organizationId());
          if (organization != null) {
            repository.leave(organization, profile.getUserId());
            result.success(new LeaveOrganizationResponse());
          } else {
            result.error(new InvalidRequestException(
                "Cannot complete request, target-organization was not found."));
          }
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<GetOrganizationResponse> addOrganizationApiKey(
      AddOrganizationApiKeyRequest request) {
    return Mono.create(result -> {
      try {

        requireNonNull(request, request.organizationId(), request.token());
        Objects.requireNonNull(request.apiKeyName(), "apiKeyName is a required argument");

        final Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          final Organization org = repository.getOrganization(request.organizationId());

          ApiKey apiKey = JwtApiKey.builder().origin("account-service")
              .subject(org.id())
              .name(request.apiKeyName())
              .claims(request.claims())
              .id(org.id())
              .build(org.secretKey());
          ApiKey[] apiKeys = Arrays.copyOf(org.apiKeys(),
              org.apiKeys().length + 1);
          apiKeys[org.apiKeys().length] = apiKey;
          Organization newOrg = Organization.builder().apiKey(apiKeys).copy(org);
          repository.updateOrganizationDetails(profile, org, newOrg);
          result.success(new GetOrganizationResponse(newOrg.id(), newOrg.name(),
              newOrg.apiKeys(), newOrg.email(),
              newOrg.ownerId()));
        } else {
          result.error(new InvalidAuthenticationToken());
        }

      } catch (Throwable ex) {
        result.error(ex);
      }
    });
  }


  @Override
  public Mono<GetOrganizationResponse> deleteOrganizationApiKey(
      DeleteOrganizationApiKeyRequest request) {
    requireNonNull(request, request.organizationId(), request.token());
    Objects.requireNonNull(request.apiKeyName());

    return Mono.create(result -> {
      try {
        final Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          final Organization org = repository.getOrganization(request.organizationId());
          List<ApiKey> apiKeys = Arrays.asList(org.apiKeys());
          Organization newOrg = Organization.builder().apiKey(apiKeys.stream()
              .filter(api -> !api.name().equals(request.apiKeyName())).toArray(
                  ApiKey[]::new)).copy(org);
          repository.updateOrganizationDetails(profile, org, newOrg);
          result.success(new GetOrganizationResponse(newOrg.id(), newOrg.name(),
              newOrg.apiKeys(), newOrg.email(),
              newOrg.ownerId()));
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
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
