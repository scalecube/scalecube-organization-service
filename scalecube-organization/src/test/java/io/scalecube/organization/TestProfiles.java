package io.scalecube.organization;

import io.scalecube.security.Profile;

public interface TestProfiles {

  Profile USER_1 =
      Profile.builder()
          .userId("USER_1")
          .email("user1@gmail.com")
          .emailVerified(true)
          .name("user1")
          .familyName("fname")
          .givenName("lname")
          .build();

  Profile USER_2 =
      Profile.builder()
          .userId("USER_2")
          .email("user2@gmail.com")
          .emailVerified(true)
          .name("user2")
          .familyName("fname")
          .givenName("lname")
          .build();
}
