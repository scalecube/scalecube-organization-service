package io.scalecube.organization.server;

import io.scalecube.organization.tokens.Auth0PublicKeyProvider;
import io.scalecube.organization.tokens.InvalidTokenException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.TokenVerifierImpl;
import io.scalecube.security.api.Profile;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RunnerUtil {

  /**
   * In case environment variable: {@code mockTokenVerifier="true"} it will be mocked with value of
   * environment variable: {@code mockTokenVerifierValue} or {@code "AUTH0_TOKEN"} by default.
   *
   * @return Token verifier
   */
  public static TokenVerifier getTokenVerifier() {
    if (Boolean.valueOf(System.getenv("mockTokenVerifier"))) {
      return mockTokenVerifier();
    }

    return new TokenVerifierImpl(new Auth0PublicKeyProvider());
  }

  private static TokenVerifier mockTokenVerifier() {
    String mockToken = Optional.ofNullable(System.getenv("mockTokenVerifierValue"))
        .orElse("AUTH0_TOKEN_MOCK");

    return token -> {
      if (token != null && token.token() != null && token.token().equals(mockToken)) {

        Map claims = new HashMap<>();
        claims.put("aud", "https://anytest.auth0.com/api/v2/");
        claims.put("role", "Owner");

        return Profile.builder()
            .userId("Ex5ToloIfQ7G7fPDJdatZdj4Pn2K36Aw@clients")
            .email("any@testemail.any")
            .email(null)
            .emailVerified(false)
            .name(null)
            .familyName(null)
            .givenName(null)
            .claims(claims).build();
      }
      throw new InvalidTokenException("Token verification failed");
    };
  }
}
