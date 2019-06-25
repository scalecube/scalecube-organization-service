package io.scalecube.organization.scenario;

import io.scalecube.security.api.Profile;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

final class TestProfiles {

  private TestProfiles() {
    // do not instantiate
  }

  static Profile generateProfile() {
    String user = "user_" + RandomStringUtils.randomAlphabetic(3);

    return Profile.builder()
        .userId(user)
        .email(user + "@scalecube.io")
        .emailVerified(true)
        .name(user)
        .familyName("fname")
        .givenName("lname")
        .build();
  }
}
