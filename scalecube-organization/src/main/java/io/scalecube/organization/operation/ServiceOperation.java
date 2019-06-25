package io.scalecube.organization.operation;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.NotAnOrganizationMemberException;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationNotFoundException;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.ServiceOperationException;
import io.scalecube.account.api.Token;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.security.api.Profile;
import java.util.Objects;
import java.util.function.Predicate;
import reactor.core.publisher.Mono;

/**
 * Represents a service operation.
 *
 * @param <I> Operation execution input
 * @param <O> Operation execution output
 */
public abstract class ServiceOperation<I, O> {

  private final TokenVerifier tokenVerifier;
  private final OrganizationsRepository repository;

  protected ServiceOperation(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    this.tokenVerifier = Objects.requireNonNull(tokenVerifier, "tokenVerifier");
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  /**
   * Executes the request argument.
   *
   * @param request the request to execute
   * @return response as a result of the request execution
   * @throws ServiceOperationException in case of an error during request execution
   */
  public Mono<O> execute(I request) throws ServiceOperationException {
    return Mono.fromRunnable(
        () -> Objects.requireNonNull(request, "request is a required argument"))
        .then(Mono.fromCallable(() -> getToken(request)))
        .flatMap(this::verifyToken)
        .map(profile -> new OperationServiceContext(profile, repository))
        .flatMap(context -> validate(request, context).then(process(request, context)))
        .onErrorMap(th -> new ServiceOperationException(request.toString(), th));
  }

  protected Mono<Void> validate(I request, OperationServiceContext context) {
    return Mono.empty();
  }

  protected abstract Token getToken(I request);

  private Mono<Profile> verifyToken(Token token) {
    return tokenVerifier
        .verify(token)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new InvalidAuthenticationToken())));
  }

  protected abstract Mono<O> process(I request, OperationServiceContext context);

  protected Mono<Organization> getOrganization(String id) throws OrganizationNotFoundException {
    return Mono.fromRunnable(() -> Objects.requireNonNull(repository, "repository"))
        .then(Mono.defer(() -> repository.findById(id)))
        .switchIfEmpty(Mono.defer(() -> Mono.error(new OrganizationNotFoundException(id))));
  }

  protected GetOrganizationResponse getOrganizationResponse(
      Organization organization, Predicate<ApiKey> filter) {
    return new GetOrganizationResponse(organizationInfo(organization, filter));
  }

  protected OrganizationInfo.Builder organizationInfo(
      Organization organization, Predicate<ApiKey> filter) {
    return OrganizationInfo.builder()
        .id(organization.id())
        .name(organization.name())
        .apiKeys(organization.apiKeys().stream().filter(filter).toArray(ApiKey[]::new))
        .email(organization.email());
  }

  protected static void requireNonNullOrEmpty(Object object, String message) {
    Objects.requireNonNull(object, message);

    if (object.toString().length() == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  protected void checkMemberAccess(Organization organization, Profile profile)
      throws AccessPermissionException, EntityNotFoundException {

    if (!isOwner(organization, profile) && !organization.isMember(profile.userId())) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', is not a member of organization: '%s'",
              profile.name(), profile.userId(), organization.id()));
    }
  }

  protected boolean isOwner(Organization organization, Profile profile)
      throws EntityNotFoundException {
    return isInRole(profile.userId(), organization, Role.Owner);
  }

  protected boolean isLastOwner(Organization organization, String userId)
      throws EntityNotFoundException {
    return organization.members().stream()
        .filter(member -> !member.id().equals(userId))
        .noneMatch(member -> Role.Owner.name().equals(member.role()));
  }

  protected boolean isSuperUser(Organization organization, Profile profile)
      throws EntityNotFoundException {
    return isOwner(organization, profile) || isInRole(profile.userId(), organization, Role.Admin);
  }

  protected Role getRole(String userId, Organization organization) {
    return organization.members().stream()
        .filter(i -> Objects.equals(i.id(), userId))
        .map(i -> Role.valueOf(i.role()))
        .findFirst()
        .orElse(null);
  }

  protected Role toRole(String role) {
    try {
      return Role.valueOf(role);
    } catch (Throwable ex) {
      throw new IllegalArgumentException("Unknown role: " + role);
    }
  }

  protected boolean isInRole(String userId, Organization organization, Role role)
      throws EntityNotFoundException {
    return organization.members().stream()
        .anyMatch(i -> Objects.equals(i.id(), userId) && Objects.equals(i.role(), role.toString()));
  }

  protected static void throwNotOrgOwnerException(Profile owner, Organization organization)
      throws AccessPermissionException {
    throw new AccessPermissionException(
        String.format(
            "user: '%s', name: '%s', is not in role Owner of organization: '%s'",
            owner.name(), owner.userId(), organization.name()));
  }

  protected void checkOwnerAccess(Organization organization, Profile profile)
      throws AccessPermissionException, EntityNotFoundException {
    if (!isOwner(organization, profile)) {
      throwNotOrgOwnerException(profile, organization);
    }
  }

  protected void checkSuperUserAccess(Organization organization, Profile profile)
      throws AccessPermissionException, EntityNotFoundException {
    if (!isSuperUser(organization, profile)) {
      throw new AccessPermissionException(
          String.format(
              "user: '%s', name: '%s', not in role Owner or Admin of organization: '%s'",
              profile.userId(), profile.name(), organization.name()));
    }
  }

  protected void checkLastOwner(String userId, Organization organization)
      throws EntityNotFoundException {
    if (isLastOwner(organization, userId)) {
      throw new IllegalStateException(
          String.format(
              "At least one Owner should be persisted in the organization: '%s'",
              organization.id()));
    }
  }

  protected void checkIsMember(String userId, Organization organization)
      throws NotAnOrganizationMemberException {
    if (!organization.isMember(userId)) {
      throw new NotAnOrganizationMemberException(
          String.format("user: %s is not a member of organization: %s", userId, organization.id()));
    }
  }

  protected Predicate<ApiKey> apiKeyFilterBy(Role role) {
    return apiKey -> {
      try {
        String roleName = apiKey.claims().get("role");
        if (roleName != null) {
          Role apiKeyRole = Role.valueOf(roleName);
          return role.isEqualsOrHigherThan(apiKeyRole);
        }
      } catch (Exception ignore) {
        // no-op
      }
      return false;
    };
  }
}
