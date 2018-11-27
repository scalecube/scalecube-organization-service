package io.scalecube.organization.repository;

import static java.util.Objects.requireNonNull;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
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
  private final UserOrganizationMembershipRepository membershipRepository;
  private final OrganizationMembersRepositoryAdmin membersAdmin;

  private OrganizationsDataAccessImpl(
      Repository<Organization, String> organizationRepository,
      UserOrganizationMembershipRepository membershipRepository,
      OrganizationMembersRepositoryAdmin repositoryAdmin) {
    this.organizations = organizationRepository;
    this.membershipRepository = membershipRepository;
    this.membersAdmin = repositoryAdmin;
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
    membersAdmin.createRepository(organization);

    try {
      membershipRepository.addMember(
          organization,
          new OrganizationMember(organization.ownerId(), Role.Owner.toString()));
      return organizations.save(organization.id(), organization);
    } catch (Throwable throwable) {
      // rollback
      membersAdmin.deleteRepository(organization);
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
      throws EntityNotFoundException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    verifyOrganizationExists(organization);
    membersAdmin.deleteRepository(organization);
    organizations.deleteById(organization.id());
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
  public void updateOrganizationDetails(Profile owner, Organization org, Organization update) {
    requireNonNullProfile(owner);
    requireNonNullOrganization(org);
    requireNonNullOrganization(update);

    if (Objects.equals(org.id(), update.id())) {
      organizations.save(org.id(), update);
    }
  }

  @Override
  public Collection<OrganizationMember> getOrganizationMembers(
      Organization organization) {
    requireNonNull(organization);
    return membershipRepository.getMembers(organization);
  }

  @Override
  public void invite(Profile owner, Organization organization, String userId)
      throws EntityNotFoundException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    verifyOrganizationExists(organization);

    addMember(userId, organization, owner.getUserId().equals(userId)
        ? Role.Owner
        : Role.Member);
  }

  private void addMember(String userId, Organization organization, Role role) {
    if (!isMember(userId, organization)) {
      membershipRepository.addMember(organization,
          new OrganizationMember(userId, role.toString()));
    }
  }

  @Override
  public void kickout(Profile owner, Organization organization, String userId)
      throws EntityNotFoundException {
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    verifyOrganizationExists(organization);

    if (membershipRepository.isMember(userId, organization)) {
      leave(organization, userId);
    }
  }

  @Override
  public void leave(Organization organization, String userId) throws EntityNotFoundException {
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    verifyOrganizationExists(organization);

    if (membershipRepository.isMember(userId, organization)) {
      membershipRepository.removeMember(userId, organization);
    }
  }

  @Override
  public boolean isMember(String userId, Organization organization) {
    return membershipRepository.isMember(userId, organization);
  }

  @Override
  public void updateOrganizationMemberRole(Organization organization, String userId, String role) {
    if (membershipRepository.isMember(userId, organization)) {
      membershipRepository.removeMember(userId, organization);
      membershipRepository.addMember(organization, new OrganizationMember(userId, role));
    }
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
