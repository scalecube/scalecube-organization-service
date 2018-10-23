package io.scalecube.organization.repository;

import static java.util.Objects.requireNonNull;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.exception.InvalidInputException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.security.Profile;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class OrganizationsDataAccessImpl implements OrganizationsDataAccess {

  private final Repository<Organization, String> organizations;
  private final UserOrganizationMembershipRepository organizationMembershipRepository;
  private final OrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin;

  private OrganizationsDataAccessImpl(
      Repository<Organization, String> organizationRepository,
      UserOrganizationMembershipRepository membershipRepository,
      OrganizationMembersRepositoryAdmin repositoryAdmin) {
    this.organizations = organizationRepository;
    this.organizationMembershipRepository = membershipRepository;
    this.organizationMembersRepositoryAdmin = repositoryAdmin;
  }

  private static void requireNonNullId(String id) {
    requireNonNull(id, "id cannot be null");
  }

  private static void requireNonNullProfile(Profile profile) {
    requireNonNull(profile, "Profile cannot be null");
  }

  private static void requireNonNullOrganization(Organization org) {
    requireNonNull(org, "Organization cannot be null");
    requireNonNull(org.id(), "Organization Id cannot be null");
  }

  public static Builder builder() {
    return new Builder();
  }


  @Override
  public Organization getOrganization(String id) throws EntityNotFoundException {
    requireNonNullId(id);
    return organizations.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
  }

  @Override
  public Organization createOrganization(Profile owner, Organization organization)
      throws DuplicateKeyException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    validateNewOrganizationInputs(organization);

    // create members repository for the organization
    organizationMembersRepositoryAdmin.createRepository(organization);

    try {
      return organizations.save(organization.id(), organization);
    } catch (Throwable throwable) {
      // rollback
      organizationMembersRepositoryAdmin.deleteRepository(organization);
      throw throwable;
    }
  }

  private void validateNewOrganizationInputs(Organization organization) {
    if (organization.id() == null || organization.id().length() == 0) {
      throw new InvalidInputException("Organization id cannot be empty");
    }

    if (organization.name() == null || organization.name().length() == 0) {
      throw new InvalidInputException("Organization name cannot be empty");
    }

    if (!organization.name().matches("^[.%a-zA-Z0-9_-]*$")) {
      throw new InvalidInputException(
          "name can only contain characters in range A-Z, a-z, 0-9 as well as "
              + "underscore, period, dash & percent.");
    }

    if (organizations.existByProperty("name", organization.name())) {
      throw new NameAlreadyInUseException(
          String.format("Organization name: '%s' already in use.",
              organization.name()));
    }

    if (organizations.existsById(organization.id())) {
      throw new DuplicateKeyException(organization.id());
    }
  }

  @Override
  public void deleteOrganization(Profile owner, Organization organization)
      throws EntityNotFoundException, AccessPermissionException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    verifyOrganizationExists(organization);

    if (isOwner(organization, owner)) {
      organizationMembersRepositoryAdmin.deleteRepository(organization);
      organizations.deleteById(organization.id());
    } else {
      throwNotOrgOwnerException(owner, organization);
    }
  }

  @Override
  public Collection<Organization> getUserMembership(String userId) {
    requireNonNull(userId, "userId");
    return Collections.unmodifiableCollection(
        StreamSupport.stream(organizations.findAll().spliterator(), false)
            .filter(org -> isMember(userId, org))
            .collect(Collectors.toList()));
  }



  @Override
  public void updateOrganizationDetails(Profile owner, Organization org, Organization update)
      throws AccessPermissionException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(org);
    requireNonNullOrganization(update);

    if (!isOwner(org, owner)) {
      throwNotOrgOwnerException(owner, org);
    }

    if (Objects.equals(org.id(), update.id())) {
      organizations.save(org.id(), update);
    }
  }

  @Override
  public Collection<OrganizationMember> getOrganizationMembers(Profile caller,
      Organization organization)
      throws AccessPermissionException {
    requireNonNullProfile(caller);
    requireNonNull(organization);
    if (!isOwner(organization, caller) && !isMember(caller.getUserId(), organization)) {
      throw new AccessPermissionException(
          String.format("user: '%s', name: '%s', is not an owner or member of organization: '%s'",
          caller.getName(), caller.getUserId(), organization.id()));
    }
    return organizationMembershipRepository.getMembers(organization);
  }

  @Override
  public void invite(Profile owner, Organization organization, String userId)
      throws AccessPermissionException, EntityNotFoundException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    verifyOrganizationExists(organization);

    if (isOwner(organization, owner)) {
      addMemberToOrg(userId, organization, owner.getUserId().equals(userId)
          ? Role.Owner
          : Role.Member);
    } else {
      throwNotOrgOwnerException(owner, organization);
    }
  }

  private void addMemberToOrg(String userId, Organization organization, Role role) {
    if (!isMember(userId, organization)) {
      organizationMembershipRepository.addMember(organization,
          new OrganizationMember(userId, role.toString()));
    }
  }

  private void throwNotOrgOwnerException(Profile owner, Organization org)
      throws AccessPermissionException {
    throw new AccessPermissionException(
        String.format("user: '%s', name: '%s', is not an owner of organization: '%s'",
            owner.getName(), owner.getUserId(), org.id()));
  }

  @Override
  public void kickout(Profile owner, Organization organization, String userId)
      throws EntityNotFoundException {
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    verifyOrganizationExists(organization);

    if (isOwner(organization, owner) && organizationMembershipRepository
        .isMember(userId, organization)) {
      leave(organization, userId);
    }
  }

  private boolean isOwner(Organization org, Profile profile) {
    return Objects.equals(org.ownerId(), profile.getUserId());
  }

  @Override
  public void leave(Organization organization, String userId) throws EntityNotFoundException {
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    verifyOrganizationExists(organization);

    if (organizationMembershipRepository.isMember(userId, organization)) {
      organizationMembershipRepository.removeMember(userId, organization);
    }
  }

  @Override
  public boolean isMember(String userId, Organization organization) {
    return organizationMembershipRepository.isMember(userId, organization);
  }

  private void verifyOrganizationExists(Organization organization)
      throws EntityNotFoundException {
    if (!organizations.existsById(organization.id())) {
      throw new EntityNotFoundException(organization.id());
    }
  }

  public static final class Builder {

    private Repository<Organization, String> organizationRepository;
    private UserOrganizationMembershipRepository membershipRepository;
    private OrganizationMembersRepositoryAdmin repositoryAdmin;

    /**
     * Constructs an instance of OrganizationsDataAccess with this builder setup arguments.
     *
     * @return An OrganizationsDataAccess instance object.
     */
    public OrganizationsDataAccess build() {
      requireNonNull(organizationRepository, "organizationRepository");
      requireNonNull(membershipRepository, "membershipRepository");
      requireNonNull(repositoryAdmin, "repositoryAdmin");
      return new OrganizationsDataAccessImpl(organizationRepository,
          membershipRepository,
          repositoryAdmin);
    }

    public Builder organizations(Repository<Organization, String> organizationRepository) {
      this.organizationRepository = organizationRepository;
      return this;
    }


    public Builder members(UserOrganizationMembershipRepository membershipRepository) {
      this.membershipRepository = membershipRepository;
      return this;
    }

    public Builder repositoryAdmin(OrganizationMembersRepositoryAdmin repositoryAdmin) {
      this.repositoryAdmin = repositoryAdmin;
      return this;
    }
  }
}
