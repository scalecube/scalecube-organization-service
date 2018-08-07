package io.scalecube.organization;

import com.google.common.collect.Lists;
import io.scalecube.account.api.*;
import io.scalecube.account.tokens.IdGenerator;
import io.scalecube.account.tokens.JwtApiKey;
import io.scalecube.account.tokens.TokenVerification;
import io.scalecube.account.tokens.TokenVerifier;
import io.scalecube.organization.repository.*;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * OrganizationService interface implementation.
 */
public class OrganizationServiceImpl implements OrganizationService {
    private final static Logger LOG = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    private final TokenVerifier tokenVerifier;
    private final OrganizationsDataAccess repository;

    private OrganizationServiceImpl(OrganizationsDataAccess repository, TokenVerifier tokenVerifier) {
        this.repository = repository;
        this.tokenVerifier = tokenVerifier;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Mono<CreateOrganizationResponse> createOrganization(CreateOrganizationRequest request) {
        checkNotNull(request);

        return Mono.create(result -> {
            try {
                final User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    final String secretKey = IdGenerator.generateId();
                    final Organization newOrg = repository.createOrganization(user, Organization.builder()
                            .id(IdGenerator.generateId())
                            .name(request.name())
                            .ownerId(user.id())
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
    public Mono<GetMembershipResponse> getUserOrganizationsMembership(GetMembershipRequest request) {
        checkNotNull(request);
        checkNotNull(request.token());

        return Mono.create(result -> {
            Collection<Organization> results;
            try {
                User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    results = repository.getUserMembership(user);
                    final List<OrganizationInfo> infos = results.stream().map(item ->
                            new OrganizationInfo(item.id(),
                                    item.name(),
                                    item.apiKeys(),
                                    item.email(),
                                    item.ownerId())).collect(Collectors.toList());
                    result.success(new GetMembershipResponse(infos.toArray(new OrganizationInfo[results.size()])));
                } else {
                    result.error(new InvalidAuthenticationToken());
                }
            } catch (Exception ex) {
                result.error(ex);
            }
        });
    }

    private boolean isUserExists(User user) {
        try {
            return repository.getUser(user.id()) != null;
        } catch (EntityNotFoundException e) {
            LOG.error("Error: user id: '{}', name:'{}' not found", user.id(), user.name());
        }
        return false;
    }

    @Override
    public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());

        return Mono.create(result -> {
            Organization organization;
            try {
                User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    organization = repository.getOrganization(request.organizationId());
                    if (organization != null) {
                        result.success(
                                new GetOrganizationResponse(organization.id(), organization.name(), organization.apiKeys(),
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
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());

        return Mono.create(result -> {
            try {
                final User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    final Organization org = repository.getOrganization(request.organizationId());
                    if (org != null) {
                        repository.deleteOrganization(user, org);
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
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());

        return Mono.create(result -> {
            try {
                final User owner = tokenVerifier.verify(request.token());
                if (owner != null && isUserExists(owner)) {
                    final Organization org = repository.getOrganization(request.organizationId());
                    if (org != null) {

                        Organization org2 = Organization.builder()
                                .name(request.name())
                                .email(request.email())
                                .copy(org);

                        repository.updateOrganizationDetails(owner, org, org2);
                        result.success(new UpdateOrganizationResponse(org2.id(), org2.name(), org2.apiKeys(),
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
    public Mono<GetOrganizationMembersResponse> getOrganizationMembers(GetOrganizationMembersRequest request) {
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());

        return Mono.create(result -> {
            Collection<OrganizationMember> organizationMembers;
            try {
                User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    organizationMembers = repository.getOrganizationMembers(request.organizationId());
                    result.success(
                            new GetOrganizationMembersResponse(
                                    organizationMembers
                                            .toArray(new OrganizationMember[organizationMembers.size()])));
                } else {
                    result.error(new InvalidAuthenticationToken());
                }
            } catch (Exception ex) {
                result.error(ex);
            }
        });
    }

    @Override
    public Mono<InviteOrganizationMemberResponse> inviteMember(InviteOrganizationMemberRequest request) {
        return Mono.create(result -> {
            try {
                User owner = tokenVerifier.verify(request.token());
                if (owner != null && isUserExists(owner)) {
                    Organization organization = repository.getOrganization(request.organizationId());
                    User user = repository.getUser(request.userId());
                    if (organization != null && user != null) {
                        repository.invite(owner, organization, user);
                        result.success(new InviteOrganizationMemberResponse());
                    } else {
                        result.error(new InvalidRequestException(
                                "Cannot complete request, target-organization or target-user was not found."));
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
    public Mono<KickoutOrganizationMemberResponse> kickoutMember(KickoutOrganizationMemberRequest request) {
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());
        checkNotNull(request.userId());

        return Mono.create(result -> {
            try {
                User owner = tokenVerifier.verify(request.token());
                if (owner != null && isUserExists(owner)) {
                    Organization organization = repository.getOrganization(request.organizationId());
                    User user = repository.getUser(request.userId());
                    if (organization != null && user != null) {
                        repository.kickout(owner, organization, user);
                        result.success(new KickoutOrganizationMemberResponse());
                    } else {
                        result.error(new InvalidRequestException(
                                "Cannot complete request, target-organization or target-user was not found."));
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
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());

        return Mono.create(result -> {
            try {
                User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    Organization organization = repository.getOrganization(request.organizationId());
                    if (organization != null) {
                        repository.leave(organization, user);
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
    public Mono<GetOrganizationResponse> addOrganizationApiKey(AddOrganizationApiKeyRequest request) {
        return Mono.create(result -> {
            try {
                checkNotNull(request);
                checkNotNull(request.organizationId(), "organizationId is a required argument");
                checkNotNull(request.token(), "token is a required argument");
                checkNotNull(request.apiKeyName(), "apiKeyName is a required argument");

                final User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    final Organization org = repository.getOrganization(request.organizationId());

                    ApiKey apiKey = JwtApiKey.builder().origin("account-service")
                            .subject(org.id())
                            .name(request.apiKeyName())
                            .claims(request.claims())
                            .id(org.id())
                            .build(org.secretKey());
                    ApiKey[] apiKeys = Lists.asList(apiKey, org.apiKeys()).toArray(new ApiKey[0]);

                    Organization newOrg = Organization.builder().apiKey(apiKeys).copy(org);
                    repository.updateOrganizationDetails(user, org, newOrg);
                    result.success(new GetOrganizationResponse(newOrg.id(), newOrg.name(), newOrg.apiKeys(), newOrg.email(),
                            newOrg.ownerId()));
                } else {
                    result.error(new InvalidAuthenticationToken());
                }

            } catch (Throwable ex) {
                ex.printStackTrace();
                result.error(ex);
            }
        });
    }


    @Override
    public Mono<GetOrganizationResponse> deleteOrganizationApiKey(DeleteOrganizationApiKeyRequest request) {
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());
        checkNotNull(request.apiKeyName());

        return Mono.create(result -> {
            try {
                final User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    final Organization org = repository.getOrganization(request.organizationId());
                    List<ApiKey> apiKeys = Arrays.asList(org.apiKeys());
                    Organization newOrg = Organization.builder().apiKey(apiKeys.stream()
                            .filter(api -> !api.name().equals(request.apiKeyName())).toArray(ApiKey[]::new)).copy(org);
                    repository.updateOrganizationDetails(user, org, newOrg);
                    result.success(new GetOrganizationResponse(newOrg.id(), newOrg.name(), newOrg.apiKeys(), newOrg.email(),
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
        private Repository<User, String> userRepository;
        private TokenVerifier tokenVerifier;
        private UserOrganizationMembershipRepository organizationMembershipRepository;
        private OrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin;

        public OrganizationService build() {
            OrganizationsDataAccess repository = new OrganizationsDataAccessImpl(
                    organizationRepository,
                    userRepository,
                    organizationMembershipRepository,
                    organizationMembersRepositoryAdmin);
            return new OrganizationServiceImpl(repository, tokenVerifier == null
                    ? new TokenVerification(repository)
                    : tokenVerifier);
        }

        public Builder organizationRepository(Repository<Organization, String> organizationRepository) {
            this.organizationRepository = organizationRepository;
            return this;
        }

        public Builder userRepository(Repository<User, String> userRepository) {
            this.userRepository = userRepository;
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

        public Builder tokenVerifier(TokenVerifier tokenVerifier) {
            this.tokenVerifier = tokenVerifier;
            return this;
        }
    }
}
