package io.scalecube.organization.repository;

import io.scalecube.organization.Organization;

/**
 * An abstraction to administrative operations on the Organization members repository.
 * Whenever an organization is created, a side-car bucket named the ORG_ID-members gets
 * auto created.
 */
public interface OrganizationMembersRepositoryAdmin {

  /**
   * Creates a members repository for the <code>organization</code> argument.
   *
   * @param organization The organization for which a members repository should be created.
   */
  void createRepository(Organization organization);


  /**
   * Deletes a members repository of the <code>organization</code> argument.
   *
   * @param organization The organization for which the members repository should be deleted.
   */
  void deleteRepository(Organization organization);
}
