package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;

public class CouchbaseRepositoryFactory {
    public static Repository<Organization, String> organizations() {
        return new CouchbaseOrganizationRepository();
    }

    public static Repository<User, String> users() {
        return new CouchbaseUserRepository();
    }

    public static UserOrganizationMembershipRepository organizationMembers() {
        return new CouchbaseUserOrganizationMembershipRepository();
    }

    public static CouchbaseOrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin() {
        return new CouchbaseOrganizationMembersRepositoryAdmin.Builder().build();
    }
}
