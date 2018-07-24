package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.User;
import io.scalecube.account.db.AccessPermissionException;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;

public class OrganizationsDataAccessImpl implements OrganizationsDataAccess {
    private final Repository<Organization, String> organizations;
    private final Repository<User, String> users;

    public OrganizationsDataAccessImpl(
            Repository<Organization, String> organizationRepository,
            Repository<User, String> userRepository) {
        this.organizations = organizationRepository;
        this.users = userRepository;
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
        if (organizations.existsById(organization.id())) {
            throw new DuplicateKeyException(organization.id());
        }
        addMemberToOrg(owner, organization, Role.Owner);
        return organizations.save(organization.id(), organization);
    }

    @Override
    public void deleteOrganization(User owner, Organization organization)
            throws EntityNotFoundException, AccessPermissionException {
        checkNotNull(owner);
        checkNotNull(organization);
        checkOrganizationExists(organization);

        boolean isCallerOrgOwner = owner.id().equals(organization.ownerId());

        if (isCallerOrgOwner) {
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
                .filter(org -> isOrgMember(user.id(), org))
                .collect(Collectors.toList()));
    }

    private boolean isOrgMember(String userId, Organization org) {
        return org
                .members()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .stream()
                .anyMatch(userId::equals);
    }

    @Override
    public void updateOrganizationDetails(User owner, Organization org, Organization update)
            throws AccessPermissionException {
        checkNotNull(owner);
        checkNotNull(org);
        checkNotNull(update);

        if (!org.ownerId().equals(owner.id())) {
            throwNotOrgOwnerException(owner, org);
        }

        if (org.id().equals(update.id())) {
            organizations.save(org.id(), update);
        }
    }

    @Override
    public Collection<OrganizationMember> getOrganizationMembers(String orgId)
            throws EntityNotFoundException {
        checkNotNull(orgId);

        return getOrganization(orgId).members()
                .entrySet()
                .stream()
                .map((e) -> getOrganizationMembers(orgId, e.getKey(), e.getValue()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<OrganizationMember> getOrganizationMembers(String orgId, String role, List<String> userIds) {
        return userIds.stream().map((id)-> {
            try {
                return new OrganizationMember(getUser(id), role);
            } catch (EntityNotFoundException e) {e.printStackTrace();}
            return null;
        }).collect(Collectors.toList());
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
            organizations.save(organization.id(), organization);
        } else {
            throwNotOrgOwnerException(owner, organization);
        }
    }

    private void addMemberToOrg(User user, Organization organization, Role role) {
        if (!isOrgMember(user.id(), organization)) {
            organization.members().putIfAbsent(role.toString(), new ArrayList<>());
            organization.members().get(role.toString()).add(user.id());
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

        if (isOwner(organization, owner) && isMember(organization, user)) {
            leave(organization, user);
        }
    }

    private boolean isOwner(Organization organization, User user) {
        return getMembers(organization, Role.Owner)
                .stream()
                .anyMatch(u -> u.id().equals(user.id()));
    }

    private boolean isMember(Organization organization, User user) {
        return getMembers(organization, Role.Member)
                .stream()
                .anyMatch(u -> u.id().equals(user.id()));
    }

    @Override
    public void leave(Organization organization, User user) throws EntityNotFoundException {
        checkNotNull(organization);
        checkNotNull(user);
        checkOrganizationExists(organization);
        getMembersByRole(organization, isOwner(organization, user) ? Role.Owner : Role.Member).remove(user.id());
        organizations.save(organization.id(), organization);
    }

    private void checkOrganizationExists(Organization organization) throws EntityNotFoundException {
        if (!organizations.existsById(organization.id())) {
            throw new EntityNotFoundException(organization.id());
        }
    }



    private List<User> getMembers(Organization organization, Role role) {
//        List<String> members = getMembersByRole(organization, role);
//
//        return members.isEmpty()
//                ? Collections.emptyList()
//                : StreamSupport
//                    .stream(users.findAllById(members).spliterator(), false)
//                    .collect(Collectors.toList());
        throw new IllegalStateException();
    }

    private List<String> getMembersByRole(Organization organization, Role role) {
        return organization.members().getOrDefault(role.toString(), new ArrayList<>());
    }
}
