package io.scalecube.organization.repository.couchbase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.security.Profile;

import org.junit.jupiter.api.Test;


public class JacksonTranslationServiceTest {

  private final Profile testProfile = new Profile(
      "1",
      null,
      "user1@gmail.com",
      true,
      "foo",
      "fname",
      "lname",
      null);

  @Test
  public void shouldEncodeUser() {
    JacksonTranslationService service = new JacksonTranslationService();
    String s = service.encode(testProfile);
    assertNotNull(s);
  }

  @Test
  public void shouldEncodeUserOrgMembership() {
    JacksonTranslationService service = new JacksonTranslationService();
    OrganizationMember member = new OrganizationMember(testProfile.getUserId(),
        Role.Owner.toString());
    String s = service.encode(member);
    assertNotNull(s);
  }


  @Test
  public void shouldEncodeOrganization() {
    JacksonTranslationService service = new JacksonTranslationService();
    String s = service.encode(new Organization.Builder().name("myorg").build());
    assertNotNull(s);
  }

  @Test
  public void shouldDecodeOrganization() {
    JacksonTranslationService service = new JacksonTranslationService();
    String s = service.encode(new Organization.Builder().name("myorg").build());
    Organization org = service.decode(s, Organization.class);
    assertNotNull(org);
    assertThat(org.name(), is("myorg"));
  }
}