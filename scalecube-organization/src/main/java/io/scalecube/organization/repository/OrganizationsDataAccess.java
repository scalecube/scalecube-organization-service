package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.account.db.AccessPermissionException;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.util.Collection;

public interface OrganizationsDataAccess {
    User getUser(String userId) throws EntityNotFoundException;

    Organization getOrganization(String id) throws EntityNotFoundException;

    Organization createOrganization(User user, Organization organization) throws AccessPermissionException, DuplicateKeyException;

    void deleteOrganization(User owner, Organization org) throws EntityNotFoundException, AccessPermissionException;

    Collection<Organization> getUserMembership(User user);

    void updateOrganizationDetails(User owner, Organization org, Organization update) throws AccessPermissionException;

    Collection<OrganizationMember> getOrganizationMembers(String id) throws EntityNotFoundException;

    void invite(User owner, Organization organization, User user) throws AccessPermissionException, EntityNotFoundException;

    void kickout(User owner, Organization organization, User user) throws EntityNotFoundException;

    void leave(Organization organization, User user) throws EntityNotFoundException;
}
