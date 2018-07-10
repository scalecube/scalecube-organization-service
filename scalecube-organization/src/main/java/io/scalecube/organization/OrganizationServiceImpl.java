package io.scalecube.organization;

import com.google.common.collect.Lists;
import io.scalecube.account.api.*;
import io.scalecube.organization.repository.*;
import io.scalecube.account.tokens.IdGenerator;
import io.scalecube.account.tokens.JwtApiKey;
import io.scalecube.account.tokens.TokenVerification;
import io.scalecube.account.tokens.TokenVerifier;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class OrganizationServiceImpl implements OrganizationService {
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
            Collection<Organization> results = new ArrayList<>();
            try {
                User user = tokenVerifier.verify(request.token());
                if (user != null && isUserExists(user)) {
                    results = repository.getUserMembership(user);
                    final List<OrganizationInfo> infos = new ArrayList<>();
                    results.forEach(item -> {
                        infos.add(new OrganizationInfo(item.id(), item.name(), item.apiKeys(), item.email(), item.ownerId()));
                    });
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
        return repository.getUser(user.id()) != null;
    }

    @Override
    public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
        checkNotNull(request);
        checkNotNull(request.organizationId());
        checkNotNull(request.token());

        return Mono.create(result -> {
            Organization organization = null;
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
            Collection<OrganizationMember> organizationMembers = null;
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
        private OrganizationRepository organizationRepository;
        private UserRepository userRepository;

        public OrganizationService build() {
            OrganizationsDataAccess repository = new OrganizationsDataAccessImpl(
                    organizationRepository,
                    userRepository
                    );
            return new OrganizationServiceImpl(repository, new TokenVerification(repository));
        }

        public Builder organizationRepository(OrganizationRepository organizationRepository) {
            this.organizationRepository = organizationRepository;
            return this;
        }

        public Builder userRepository(UserRepository userRepository) {
            this.userRepository = userRepository;
            return this;
        }
    }
}
