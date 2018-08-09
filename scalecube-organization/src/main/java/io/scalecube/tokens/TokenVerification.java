package io.scalecube.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;

public class TokenVerification implements TokenVerifier {
  private final TokenVerifier tokenVerifier = new TokenVerifierImpl();

  /**
   * Verifies the token argument.
   *
   * @param token to be verified.
   * @return Profile if token is verified or null in case its invalid token.
   * @throws Exception in case of parsing error.
   */
  public Profile verify(Token token) throws Exception {
    return tokenVerifier.verify(token);
  }
}
