package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.account.db.AccessPermissionException;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.OrganizationNotFoundException;
import io.scalecube.organization.repository.exception.UserNotFoundException;

import java.util.Collection;

public interface OrganizationsDataAccess {
    User getUser(String userId) throws UserNotFoundException;

    Organization getOrganization(String id) throws OrganizationNotFoundException;

    Organization createOrganization(User user, Organization organization) throws AccessPermissionException, DuplicateKeyException;

    void deleteOrganization(User owner, Organization org) throws OrganizationNotFoundException, AccessPermissionException;

    Collection<Organization> getUserMembership(User user);

    void updateOrganizationDetails(User owner, Organization org, Organization update) throws AccessPermissionException;

    Collection<OrganizationMember> getOrganizationMembers(String id) throws OrganizationNotFoundException;

    void invite(User owner, Organization organization, User user) throws AccessPermissionException, OrganizationNotFoundException;

    void kickout(User owner, Organization organization, User user) throws OrganizationNotFoundException;

    void leave(Organization organization, User user) throws OrganizationNotFoundException;
}
