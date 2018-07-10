package io.scalecube.account.db;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrganizationsRepositoryImpl implements OrganizationsRepository
{
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Organization> organizations = new ConcurrentHashMap<>();
    private final Map<String, Map<String, OrganizationMember>> organizationMembers = new ConcurrentHashMap<>();

    @Override
    public User getUser(String userId) {
        return users.get(userId);
    }

    @Override
    public Organization getOrganization(String id) {
        return organizations.get(id);
    }

    @Override
    public Organization createOrganization(User owner, Organization organization) {
        organizations.putIfAbsent(organization.id(), organization);
        organizationMembers.putIfAbsent(organization.id(), new ConcurrentHashMap<>());
        organizationMembers.get(organization.id()).putIfAbsent(owner.id(), new OrganizationMember(owner, "owner"));
        return organization;
    }

    @Override
    public void deleteOrganization(User owner, Organization org) {
        if (owner.id().equals(org.ownerId())
                && organizationMembers.get(org.id()).size() == 1
                && organizationMembers.get(org.id()).containsKey(owner.id())) {

            organizations.remove(org.id());
            organizationMembers.remove(org.id());
        }
    }

    @Override
    public Collection<Organization> getUserMembership(User user) {
        return Collections.unmodifiableCollection(organizations.values().stream()
                .filter(org -> isOrganizationMember(user, org))
                .collect(Collectors.toList()));
    }

    @Override
    public void updateOrganizationDetails(User owner, Organization org, Organization update) {
        if (org.id().equals(update.id())) {
            organizations.put(org.id(), update);
        }
    }

    @Override
    public Collection<OrganizationMember> getOrganizationMembers(String id) {
        return organizationMembers.containsKey(id)
                ? organizationMembers.get(id).values()
                : Collections.EMPTY_LIST;
    }

    public void invite(User owner, Organization organization, final User user) throws AccessPermissionException {
        if (isOwner(organization, owner)) {
            final Map<String, OrganizationMember> members =
                    organizationMembers.get(organization.id());

            if (owner.id().equals(user.id())) {
                members.putIfAbsent(user.id(), new OrganizationMember(user, "owner"));
            } else {
                members.putIfAbsent(user.id(), new OrganizationMember(user, "member"));
            }
        } else {
            throw new AccessPermissionException(
                    "user: " + owner.name() + " id: " + owner.id() + " is not an owner of organization: " + organization.id());
        }
    }

    @Override
    public void kickout(User owner, Organization organization, User user) {
        if (isOwner(organization, owner) && isOrganizationMember(user, organization)) {
            leave(organization, user);
        }
    }

    @Override
    public void leave(Organization organization, User user) {
        final Map<String, OrganizationMember> members = organizationMembers.get(organization.id());
        members.remove(user.id());
    }

    private boolean isOwner(Organization organization, User user) {
        return getOwners(organization).stream().anyMatch(u -> u.id().equals(user.id()));
    }

    private List<User> getOwners(Organization organization) {
        final Map<String, OrganizationMember> members = organizationMembers.get(organization.id());
        return Collections.unmodifiableList(
                 members.values()
                        .stream()
                        .filter(m -> m.role().equals("owner"))
                        .map(OrganizationMember::user)
                        .collect(Collectors.toList()));
    }


    private boolean isOrganizationMember(User user, Organization organization) {
        final Map<String, OrganizationMember> members = organizationMembers.get(organization.id());
        return members.containsKey(user.id());
    }
}
