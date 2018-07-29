package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.User;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;



public class JacksonTranslationServiceTest {
    private final User testUser = new User("1", "user1@gmail.com", true, "name 1",
            "http://picture.jpg", "EN", "fname", "lname", null);
    @Test
    public void shouldEncodeUser() {
        JacksonTranslationService service = new JacksonTranslationService();
        String s = service.encode(testUser);
        Assert.assertNotNull(s);
    }

    @Test
    public void shouldEncodeUserOrgMembership() {
        JacksonTranslationService service = new JacksonTranslationService();
        OrganizationMember member = new OrganizationMember(testUser, Role.Owner.toString());
        String s = service.encode(member);
        Assert.assertNotNull(s);
    }


    @Test
    public void shouldEncodeOrganization() {
        JacksonTranslationService service = new JacksonTranslationService();
        String s = service.encode(new Organization.Builder().name("myorg").build());
        Assert.assertNotNull(s);
    }

    @Test
    public void shouldDecodeOrganization() {
        JacksonTranslationService service = new JacksonTranslationService();
        String s = service.encode(new Organization.Builder().name("myorg").build());
        Organization org = service.decode(s, Organization.class);
        Assert.assertNotNull(org);
        assertThat(org.name(), is("myorg"));
    }
}