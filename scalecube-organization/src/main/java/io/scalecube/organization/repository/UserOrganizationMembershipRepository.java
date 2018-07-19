package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;

import java.util.Set;

public interface UserOrganizationMembershipRepository extends
        Repository<Set<OrganizationMember>, String> {

    void addMemberToOrganization(Organization org, OrganizationMember member);

    Set<String> getUserMembership(User user);
    void createUserOrganizationMembershipRepository(Organization organization);
}
