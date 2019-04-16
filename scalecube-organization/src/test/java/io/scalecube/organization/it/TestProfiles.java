package io.scalecube.organization.it;

import io.scalecube.security.api.Profile;

public interface TestProfiles {

  Profile USER_A =
      Profile.builder()
          .userId("USER_A")
          .email("user1@scalecube.io")
          .emailVerified(true)
          .name("user1")
          .familyName("fname")
          .givenName("lname")
          .build();

  Profile USER_B =
      Profile.builder()
          .userId("USER_B")
          .email("user2@scalecube.io")
          .emailVerified(true)
          .name("user2")
          .familyName("fname")
          .givenName("lname")
          .build();

  Profile USER_C =
      Profile.builder()
          .userId("USER_C")
          .email("user3@scalecube.io")
          .emailVerified(true)
          .name("user3")
          .familyName("fname")
          .givenName("lname")
          .build();

  Profile USER_D =
      Profile.builder()
          .userId("USER_D")
          .email("user4@scalecube.io")
          .emailVerified(true)
          .name("user4")
          .familyName("fname")
          .givenName("lname")
          .build();
}
