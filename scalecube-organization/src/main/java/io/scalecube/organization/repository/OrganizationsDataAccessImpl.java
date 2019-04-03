package io.scalecube.organization.repository;

import static java.util.Objects.requireNonNull;

import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.organization.operation.Organization;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class OrganizationsDataAccessImpl implements OrganizationsDataAccess {

  private final Repository<Organization, String> organizations;
  private final UserOrganizationMembershipRepository membershipRepository;

  /**
   * Creates instance of Organizations Data Access.
   *
   * @param organizationRepository organization repository.
   * @param membershipRepository membership repository.
   */
  public OrganizationsDataAccessImpl(
      Repository<Organization, String> organizationRepository,
      UserOrganizationMembershipRepository membershipRepository) {
    this.organizations = organizationRepository;
    this.membershipRepository = membershipRepository;
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

  @Override
  public boolean existByName(String name) {
    requireNonNullId(name);
    return organizations.existByProperty("name", name);
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

    OrganizationMember organizationOwner =
        new OrganizationMember(owner.getUserId(), Role.Owner.toString());

    return organizations.save(
        organization.id(),
        Organization.builder()
            .members(new OrganizationMember[] {organizationOwner})
            .apiKeys(organization.apiKeys())
            .copy(organization));
  }

  @Override
  public void deleteOrganization(Profile owner, Organization organization)
      throws EntityNotFoundException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    verifyOrganizationExists(organization);
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
  public Collection<OrganizationMember> getOrganizationMembers(Organization organization) {
    requireNonNull(organization);
    return membershipRepository.getMembers(organization);
  }

  @Override
  public void invite(Profile owner, Organization organization, String userId, Role role)
      throws EntityNotFoundException {
    requireNonNullProfile(owner);
    requireNonNullOrganization(organization);
    requireNonNull(userId, "userId");
    requireNonNull(role, "role");
    verifyOrganizationExists(organization);
    addMember(userId, organization, role);
  }

  private void addMember(String userId, Organization organization, Role role) {
    if (!isMember(userId, organization)) {
      membershipRepository.addMember(organization, new OrganizationMember(userId, role.toString()));
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

  private void verifyOrganizationExists(Organization organization) throws EntityNotFoundException {
    if (!organizations.existsById(organization.id())) {
      throw new EntityNotFoundException(organization.id());
    }
  }
}
