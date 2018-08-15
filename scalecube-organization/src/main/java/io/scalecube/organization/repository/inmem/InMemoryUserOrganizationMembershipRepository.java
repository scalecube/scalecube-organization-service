package io.scalecube.organization.repository.inmem;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class InMemoryUserOrganizationMembershipRepository
    implements UserOrganizationMembershipRepository {

  private final HashMap<String, Set<OrganizationMember>> map = new HashMap<>();

  @Override
  public void addMember(Organization org, OrganizationMember member) {
    map.putIfAbsent(org.id(), new HashSet<>());
    map.get(org.id()).add(member);
  }

  @Override
  public boolean isMember(String userId, Organization organization) {
    return map.containsKey(organization.id()) && map.get(organization.id()).stream()
        .anyMatch(m -> Objects.equals(m.id(), userId));
  }

  @Override
  public Collection<OrganizationMember> getMembers(Organization organization) {
    return map.containsKey(organization.id()) ? map.get(organization.id())
        : Collections.emptyList();
  }

  @Override
  public void removeMember(String userId, Organization organization) {
    if (isMember(userId, organization)) {
      Optional<OrganizationMember> member = getMember(userId, organization);
      member.ifPresent(organizationMember -> map.get(organization.id()).remove(organizationMember));
    }
  }

  @Override
  public Optional<OrganizationMember> getMember(String userId, Organization organization) {
    return isMember(userId, organization)
        ? map
        .get(organization.id())
        .stream()
        .filter(m -> Objects.equals(m.id(), userId)).findAny()
        : Optional.empty();
  }
}
