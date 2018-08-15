package io.scalecube.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;

import java.util.Objects;

public class TokenVerification implements TokenVerifier {

  private final TokenVerifier tokenVerifier = new TokenVerifierImpl();

  /**
   * Verifies the token argument.
   *
   * @param token to be verified.
   * @return Profile if token is verified or null in case its invalid token.
   * @throws InvalidTokenException in case an error.
   */
  public Profile verify(Token token) throws InvalidTokenException {
    Objects.requireNonNull(token, "token");
    Objects.requireNonNull(token.token(), "token");
    try {
      return tokenVerifier.verify(token);
    } catch (Throwable ex) {
      throw new InvalidTokenException("Token verification failed", ex);
    }
  }
}