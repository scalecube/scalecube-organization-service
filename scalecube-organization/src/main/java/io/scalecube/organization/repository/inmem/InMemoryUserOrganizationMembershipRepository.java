package io.scalecube.organization.repository.inmem;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;

import java.util.*;


public class InMemoryUserOrganizationMembershipRepository
        implements UserOrganizationMembershipRepository {
    private final HashMap<String, Set<OrganizationMember>> map = new HashMap<>();

    @Override
    public void addMember(Organization org, OrganizationMember member) {
        map.putIfAbsent(org.id(), new HashSet<>());
        map.get(org.id()).add(member);
    }

    @Override
    public boolean isMember(User user, Organization organization) {
        return map.containsKey(organization.id()) && map.get(organization.id()).stream()
                .anyMatch(m -> Objects.equals(m.user().id(), user.id()));
    }

    @Override
    public Collection<OrganizationMember> getMembers(Organization organization) {
        return map.containsKey(organization.id()) ? map.get(organization.id()) : Collections.emptyList();
    }

    @Override
    public void removeMember(User user, Organization organization) {
        if (isMember(user, organization)) {
            Optional<OrganizationMember> member = getMember(user, organization);
            if (member.isPresent()) {
                map.get(organization.id()).remove(member.get());
            }
        }
    }

    @Override
    public Optional<OrganizationMember> getMember(User user, Organization organization) {
        return isMember(user, organization)
                ? map
                    .get(organization.id())
                    .stream()
                    .filter(m -> Objects.equals(m.user().id(), user.id())).findAny()
                : Optional.empty();
    }
}
