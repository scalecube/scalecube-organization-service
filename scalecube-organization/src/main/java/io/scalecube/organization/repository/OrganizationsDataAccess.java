package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.DuplicateKeyException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;

import java.util.Collection;

/**
 * Data access abstraction to organizations data providers. Implementing classes may persist the
 * data in different ways (e.g. in-memory, file on disk, DB). Data persistence is not enforced and
 * therefore not guaranteed and it is in the scope of the implementing class.
 */
public interface OrganizationsDataAccess {

  /**
   * Returns an Organization object corresponding to the <code>id</code> argument in the underlying
   * data provider.
   *
   * @param id The organization id criteria.
   * @return An organization object.
   * @throws EntityNotFoundException in case an entity with a corresponding id is not found in the
   *     underlying data provider.
   */
  Organization getOrganization(String id) throws EntityNotFoundException;

  /**
   * Creates an Organization entry in the underlying data provider with the
   * <code>organization</code> and <code>owner</code> arguments.
   *
   * @param owner Organization owner.
   * @param organization The new organization to be created.
   * @return An organization object.
   * @throws AccessPermissionException In case of insufficient privileges.
   * @throws DuplicateKeyException In case an organization with the same id already exists in the
   *     underlying data provider.
   */
  Organization createOrganization(Profile owner, Organization organization)
      throws AccessPermissionException, DuplicateKeyException;

  /**
   * Deletes an Organization entry in the underlying data provider with the corresponding to the
   * <code>organization</code> argument.
   *
   * @param owner Organization owner.
   * @param organization The organization to be deleted.
   * @throws AccessPermissionException In case of insufficient privileges.
   * @throws EntityNotFoundException In case an organization with the same id does not exists in the
   *     underlying data provider.
   */
  void deleteOrganization(Profile owner, Organization organization)
      throws EntityNotFoundException, AccessPermissionException;

  /**
   * Returns a list of organizations of which the <code>profile</code> argument is a member of those
   * organizations.
   *
   * @param userId The id of the profile criteria.
   * @return a list of <code>Organization</code> objects.
   */
  Collection<Organization> getUserMembership(String userId);

  /**
   * Updates the <code>source</code> organization with the <code>update</code> organization in the
   * underlying data provider.
   *
   * @param owner The organization owner.
   * @param source The organization source.
   * @param update The updated organization.
   * @throws AccessPermissionException In case of insufficient privileges.
   */
  void updateOrganizationDetails(Profile owner, Organization source, Organization update)
      throws AccessPermissionException;

  /**
   * Returns a membership list of an organization corresponding to the <code>id</code>  argument in
   * the  underlying data provider.
   *
   * @param id The organization id criteria.
   * @return A collection of <code>OrganizationMember</code> objects.
   * @throws EntityNotFoundException In case organization is not found.
   */
  Collection<OrganizationMember> getOrganizationMembers(String id) throws EntityNotFoundException;

  /**
   * Invites the <coded>profile</coded> argument to join the <code>organization</code> argument.
   *
   * @param owner The Organization owner
   * @param organization The Organization to join.
   * @param userId The invited user id.
   * @throws AccessPermissionException In case of insufficient privileges.
   * @throws EntityNotFoundException In case the organization does not exists in the underlying data
   *     provider.
   */
  void invite(Profile owner, Organization organization, String userId) throws
      AccessPermissionException, EntityNotFoundException;

  /**
   * Kicks out the <coded>profile</coded> argument from the <code>organization</code> argument.
   *
   * @param owner The Organization owner
   * @param organization The Organization from which the profile is to be kicked out.
   * @param userId The kicked out user id.
   * @throws EntityNotFoundException In case the organization does not exists in the underlying data
   *     provider.
   */
  void kickout(Profile owner, Organization organization, String userId)
      throws EntityNotFoundException;

  /**
   * Enables the <code>profile</code> to leave the <code>organization</code>.
   *
   * @param organization The organization of which the profile wishes to leave.
   * @param userId The id of the profile requesting to leave the organization.
   * @throws EntityNotFoundException In case the organization is not found in the underlying data
   *     provider.
   */
  void leave(Organization organization, String userId) throws EntityNotFoundException;
}
