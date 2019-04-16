package io.scalecube.organization.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.api.Profile;

public interface TokenVerifier {

  /**
   * Verifies the token argument.
   *
   * @param token to be verified.
   * @return Profile if token is verified or null in case its invalid token.
   * @throws InvalidTokenException in case an error.
   */
  Profile verify(Token token) throws InvalidTokenException;
}
