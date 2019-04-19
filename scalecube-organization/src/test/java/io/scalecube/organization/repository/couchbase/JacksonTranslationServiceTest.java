package io.scalecube.organization.repository.couchbase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.organization.domain.Organization;
import io.scalecube.security.api.Profile;
import org.junit.jupiter.api.Test;

class JacksonTranslationServiceTest {

  private final Profile testProfile =
      Profile.builder()
          .userId("1")
          .email("user1@gmail.com")
          .emailVerified(true)
          .name("foo")
          .familyName("fname")
          .givenName("lname")
          .build();

  @Test
  void shouldEncodeUser() {
    JacksonTranslationService service = new JacksonTranslationService();
    String s = service.encode(testProfile);
    assertNotNull(s);
  }

  @Test
  void shouldEncodeUserOrgMembership() {
    JacksonTranslationService service = new JacksonTranslationService();
    OrganizationMember member =
        new OrganizationMember(testProfile.userId(), Role.Owner.toString());
    String s = service.encode(member);
    assertNotNull(s);
  }

  @Test
  void shouldEncodeOrganization() {
    JacksonTranslationService service = new JacksonTranslationService();
    String s = service.encode(new Organization("1", "TEST-ORG", "test@scalecube.io", "1"));
    assertNotNull(s);
  }

  @Test
  void shouldDecodeOrganization() {
    JacksonTranslationService service = new JacksonTranslationService();
    String id = "org-id";
    String name = "org-name";
    String email = "test@scalecube.io";
    String ownerUserId = "owner-user-id";
    String s = service.encode(new Organization(id, name, email, ownerUserId));
    Organization org = service.decode(s, Organization.class);
    assertNotNull(org);
    assertThat(org.id(), is(id));
    assertThat(org.name(), is(name));
    assertThat(org.email(), is(email));
    assertThat(org.members().iterator().next().id(), is(ownerUserId));
  }
}
