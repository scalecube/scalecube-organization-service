package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;

import java.util.Collection;
import java.util.Optional;

public interface UserOrganizationMembershipRepository {
    void addMember(Organization org, OrganizationMember member);

    boolean isMember(User user, Organization organization);

    Collection<OrganizationMember> getMembers(Organization organization);

    void removeMember(User user, Organization organization);

    Optional<OrganizationMember> getMember(User user, Organization organization);
}
