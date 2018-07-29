package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.User;
import io.scalecube.account.db.AccessPermissionException;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.exception.InvalidInputException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class OrganizationsDataAccessImpl implements OrganizationsDataAccess {
    private final Repository<Organization, String> organizations;
    private final Repository<User, String> users;
    private final UserOrganizationMembershipRepository organizationMembershipRepository;
    private final OrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin;

    public OrganizationsDataAccessImpl(
            Repository<Organization, String> organizationRepository,
            Repository<User, String> userRepository,
            UserOrganizationMembershipRepository membershipRepository,
            OrganizationMembersRepositoryAdmin repositoryAdmin) {
        this.organizations = organizationRepository;
        this.users = userRepository;
        this.organizationMembershipRepository = membershipRepository;
        this.organizationMembersRepositoryAdmin = repositoryAdmin;
    }

    @Override
    public User getUser(String id) throws EntityNotFoundException {
        checkNotNull(id);
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
    }

    @Override
    public Organization getOrganization(String id) throws EntityNotFoundException {
        checkNotNull(id);
        return organizations.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
    }

    @Override
    public Organization createOrganization(User owner, Organization organization) throws DuplicateKeyException {
        checkNotNull(owner);
        checkNotNull(organization);
        checkNotNull(organization.id());
        validateNewOrganizationInputs(organization);

        // create members repository for the organization
        organizationMembersRepositoryAdmin.createRepository(organization);

        try {
            return organizations.save(organization.id(), organization);
        } catch (Throwable t) {
            // rollback
            organizationMembersRepositoryAdmin.deleteRepository(organization);
            throw t;
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
            throw new InvalidInputException("name can only contain characters in range A-Z, a-z, 0-9 as well as " +
                    "underscore, period, dash & percent.");
        }

        if (organizations.existByProperty("name", organization.name())) {
            throw new NameAlreadyInUseException(String.format("Organization name: '%s' already in use.",
                    organization.name()));
        }

        if (organizations.existsById(organization.id())) {
            throw new DuplicateKeyException(organization.id());
        }
    }

    @Override
    public void deleteOrganization(User owner, Organization organization)
            throws EntityNotFoundException, AccessPermissionException {
        checkNotNull(owner);
        checkNotNull(organization);
        checkOrganizationExists(organization);

        if (isOwner(organization, owner)) {
            organizationMembersRepositoryAdmin.deleteRepository(organization);
            organizations.deleteById(organization.id());
        } else {
            throwNotOrgOwnerException(owner, organization);
        }
    }

    @Override
    public Collection<Organization> getUserMembership(User user) {
        checkNotNull(user);
        return Collections.unmodifiableCollection(
                StreamSupport.stream(organizations.findAll().spliterator(), false)
                .filter(org -> isOrgMember(user, org))
                .collect(Collectors.toList()));
    }

    private boolean isOrgMember(User user, Organization org) {
        return organizationMembershipRepository.isMember(user, org);
    }

    @Override
    public void updateOrganizationDetails(User owner, Organization org, Organization update)
            throws AccessPermissionException {
        checkNotNull(owner);
        checkNotNull(org);
        checkNotNull(update);

        if (!isOwner(org, owner)){
            throwNotOrgOwnerException(owner, org);
        }

        if (Objects.equals(org.id(), update.id())) {
            organizations.save(org.id(), update);
        }
    }

    @Override
    public Collection<OrganizationMember> getOrganizationMembers(String orgId)
            throws EntityNotFoundException {
        checkNotNull(orgId);
        return organizationMembershipRepository.getMembers(getOrganization(orgId));
    }

    public void invite(User owner, Organization organization, User user)
            throws AccessPermissionException, EntityNotFoundException {
        checkNotNull(owner);
        checkNotNull(organization);
        checkNotNull(user);
        checkOrganizationExists(organization);

        if (isOwner(organization, owner)) {
            addMemberToOrg(user, organization, owner.id().equals(user.id())
                    ? Role.Owner
                    : Role.Member);
        } else {
            throwNotOrgOwnerException(owner, organization);
        }
    }

    private void addMemberToOrg(User user, Organization organization, Role role) {
        if (!isOrgMember(user, organization)) {
            organizationMembershipRepository.addMember(organization,
                    new OrganizationMember(user, role.toString()));
        }
    }

    private void throwNotOrgOwnerException(User owner, Organization org) throws AccessPermissionException {
        throw new AccessPermissionException(
                String.format("user: %s, name: %s, is not an owner of organization: %s",
                        owner.name(), owner.id(), org.id()));
    }

    @Override
    public void kickout(User owner, Organization organization, User user) throws EntityNotFoundException {
        checkNotNull(organization);
        checkNotNull(user);
        checkOrganizationExists(organization);


        if (isOwner(organization, owner) && organizationMembershipRepository.isMember(user, organization)) {
            leave(organization, user);
        }
    }

    private boolean isOwner(Organization org, User user) {
        return Objects.equals(org.ownerId(), user.id());
    }


    @Override
    public void leave(Organization organization, User user) throws EntityNotFoundException {
        checkNotNull(organization);
        checkNotNull(user);
        checkOrganizationExists(organization);

        if (organizationMembershipRepository.isMember(user, organization)) {
            organizationMembershipRepository.removeMember(user, organization);
        }
    }

    private void checkOrganizationExists(Organization organization) throws EntityNotFoundException {
        if (!organizations.existsById(organization.id())) {
            throw new EntityNotFoundException(organization.id());
        }
    }



    private List<User> getMembers(Organization organization, Role role) {
        return organizationMembershipRepository.getMembers(organization)
                .stream()
                .filter(m -> Objects.equals(m.role(), role.toString()))
                .map(m -> m.user())
                .collect(Collectors.toList());
    }
}
