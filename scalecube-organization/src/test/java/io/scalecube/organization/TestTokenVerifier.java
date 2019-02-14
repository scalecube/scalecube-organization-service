package io.scalecube.organization;

import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;
import io.scalecube.tokens.InvalidTokenException;
import io.scalecube.tokens.TokenVerifier;
import java.util.HashMap;
import java.util.Map;

class TestTokenVerifier implements TokenVerifier {

  public static final Profile USER_1 =
      Profile.builder()
          .userId("USER_1")
          .email("user1@gmail.com")
          .emailVerified(true)
          .name("user1")
          .familyName("fname")
          .givenName("lname")
          .build();

  public static final Profile USER_2 =
      Profile.builder()
          .userId("USER_2")
          .email("user2@gmail.com")
          .emailVerified(true)
          .name("user2")
          .familyName("fname")
          .givenName("lname")
          .build();

  private final Map<String, Profile> profiles;

  public TestTokenVerifier() {
    this.profiles = new HashMap<>();
    profiles.put(USER_1.getUserId(), USER_1);
    profiles.put(USER_2.getUserId(), USER_2);
  }

  public static Token token(Profile profile) {
    return new Token(profile.getUserId());
  }

  @Override
  public Profile verify(Token token) {
    Profile profile = profiles.get(token.token());
    if (profile == null) {
      throw new InvalidTokenException("Token verification failed");
    }
    return profile;
  }
}
