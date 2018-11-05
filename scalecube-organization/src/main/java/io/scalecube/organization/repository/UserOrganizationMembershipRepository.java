package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;

import java.util.Collection;
import java.util.Optional;

/**
 * An abstraction of user organization membership data access.
 */
public interface UserOrganizationMembershipRepository {

  /**
   * Adds the <code>member</code> argument to the <code>organization</code> argument, in the
   * underlying data provider.
   *
   * @param organization The organization ao add the member to.
   * @param member The member to add.
   */
  void addMember(Organization organization, OrganizationMember member);

  /**
   * Determines if the <code>tokenVerifier</code> argument is a member of the
   * <code>organization</code> argument.
   *
   * @param userId The user id criteria.
   * @param organization The organization criteria.
   * @return True if the tokenVerifier is a member of the organization; false otherwise.
   */
  boolean isMember(String userId, Organization organization);

  /**
   * Returns a list of members in the <code>organization</code> argument.
   *
   * @param organization Organization criteria.
   * @return A list of <code>OrganizationMember</code> objects.
   */
  Collection<OrganizationMember> getMembers(Organization organization);

  /**
   * Removes the <code>tokenVerifier</code> argument from the <code>organization</code> argument,
   * in the underlying data provider.
   *
   * @param organization The organization ao add the member to.
   * @param userId The id of the tokenVerifier to remove.
   */
  void removeMember(String userId, Organization organization);

  /**
   * Returns an organization membership info of the <code>tokenVerifier</code> argument in the
   * <code>organization</code> argument.
   *
   * @param userId Profile criteria.
   * @param organization Organization criteria.
   * @return <code>Optional</code> of <code>OrganizationMember</code>.
   */
  Optional<OrganizationMember> getMember(String userId, Organization organization);
}
