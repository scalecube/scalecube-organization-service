package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;

import java.util.Collection;
import java.util.Optional;

/**
 * An abstraction of user organization membership data access.
 */
public interface UserOrganizationMembershipRepository {

    /**
     * Adds the <code>member</code> argument to the <code>organization</code> argument, in the
     * underlying data provider
     * @param organization The organization ao add the member to.
     * @param member The member to add.
     */
    void addMember(Organization organization, OrganizationMember member);

    /**
     * Determines if the <code>user</code> argument is a member of the <code>organization</code>
     * argument.
     * @param user The user criteria.
     * @param organization The organization criteria.
     * @return True if the user is a member of the organization; false otherwise.
     */
    boolean isMember(User user, Organization organization);

    /**
     * Returns a list of members in the <code>organization</code> argument.
     * @param organization Organization criteria.
     * @return A list of <code>OrganizationMember</code> objects.
     */
    Collection<OrganizationMember> getMembers(Organization organization);

    /**
     * Removes the <code>user</code> argument from the <code>organization</code> argument, in the
     * underlying data provider.
     * @param organization The organization ao add the member to.
     * @param user The user to remove.
     */
    void removeMember(User user, Organization organization);

    /**
     * Returns an organization membership info of the <code>user</code> argument in the
     * <code>organization</code> argument.
     * @param user User criteria.
     * @param organization Organization criteria.
     * @return
     */
    Optional<OrganizationMember> getMember(User user, Organization organization);
}
