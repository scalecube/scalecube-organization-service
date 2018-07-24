package io.scalecube.organization.repository.inmem;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryUserOrganizationMembershipRepository
        implements UserOrganizationMembershipRepository {
    private final HashMap<String, Set<OrganizationMember>> map = new HashMap<>();

    @Override
    public Optional<Set<OrganizationMember>> findById(String orgId) {
        if (map.containsKey(orgId)) {
            Optional.empty();
        }
        return Optional.of(map.get(orgId));
    }



    @Override
    public boolean existsById(String orgId) {
        return map.containsKey(orgId);
    }

    @Override
    public Set<OrganizationMember> save(String orgId, Set<OrganizationMember> organizationMembers) {
        map.putIfAbsent(orgId, new HashSet<>());
        map.get(orgId).addAll(organizationMembers);
        return map.get(orgId);
    }

    @Override
    public void deleteById(String orgId) {
        map.remove(orgId);
    }

    @Override
    public Iterable<Set<OrganizationMember>> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public void addMemberToOrganization(Organization org, OrganizationMember member) {
        save(org.id(), new HashSet<>(Collections.singletonList(member)));
    }

    @Override
    public Set<String> getUserMembership(User user) {
        return map.entrySet()
                .stream()
                .filter((e)->e.getValue().stream().anyMatch((i)->i.user().equals(user)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void createUserOrganizationMembershipRepository(Organization organization) {
        map.putIfAbsent(organization.id(), new HashSet<>());
    }
}
