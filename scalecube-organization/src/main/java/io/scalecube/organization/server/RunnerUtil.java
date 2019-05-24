package io.scalecube.organization.server;

import io.scalecube.organization.tokens.InvalidTokenException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.security.api.Profile;
import java.util.HashMap;
import java.util.Map;

class RunnerUtil {

  /**
   * TokenVerifier mock with value of System property: {@code mockTokenVerifierValue} or {@code
   * "AUTH0_TOKEN"} otherwise by default.
   *
   * @return TokenVerifier mocked
   */
  static TokenVerifier mockTokenVerifier() {
    String mockToken = System.getProperty("mockTokenVerifierValue", "AUTH0_TOKEN_MOCK");

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
