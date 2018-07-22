package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.couchbase.CouchbaseUserOrganizationMembershipRepository;
import io.scalecube.testlib.BaseTest;
import org.junit.Test;

public class CouchbaseUserOrganizationMembershipRepositoryTest extends BaseTest {
    private final User testUser = new User("1", "user1@gmail.com", true, "name 1",
            "http://picture.jpg", "EN", "fname", "lname", null);

    @Test
    public void testInitSettings() {
        CouchbaseUserOrganizationMembershipRepository r = new CouchbaseUserOrganizationMembershipRepository();
        r.init();
    }

    @Test
    public void addMemberToOrganization() {
        CouchbaseUserOrganizationMembershipRepository r = new CouchbaseUserOrganizationMembershipRepository();
        Organization myorg = new Organization.Builder().name("myorg").build();
        r.addMemberToOrganization(myorg,
                new OrganizationMember(testUser, Role.Owner.toString()));
    }

    @Test
    public void createUserOrganizationMembershipRepository() {
        CouchbaseUserOrganizationMembershipRepository r = new CouchbaseUserOrganizationMembershipRepository();
        r.createUserOrganizationMembershipRepository(new Organization.Builder().name("myorg").build());
    }
}
