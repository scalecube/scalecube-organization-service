package io.scalecube.account.db;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;

import java.util.Collection;

public interface OrganizationsRepository {
    User getUser(String userId);

    Organization getOrganization(String id);

    Organization createOrganization(User user, Organization organization) throws AccessPermissionException;

    void deleteOrganization(User owner, Organization org);

    Collection<Organization> getUserMembership(User user);

    void updateOrganizationDetails(User owner, Organization org, Organization update);

    Collection<OrganizationMember> getOrganizationMembers(String id);

    void invite(User owner, Organization organization, User user) throws AccessPermissionException;

    void kickout(User owner, Organization organization, User user);

    void leave(Organization organization, User user);
}
