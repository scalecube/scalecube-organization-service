package io.scalecube.organization.tokens;

import java.security.PublicKey;

/** Abstraction of public key source, used to verify token signature. */
public interface PublicKeyProvider {

  /**
   * Returns a public key which used to sign the <code>token</code> argument.
   *
   * @param token The signed token bearing information on the public key which was used to sign the
   *     token.
   * @return A public key.
   * @throws InvalidTokenException In case of exception throws while trying to extract the public
   *     key from the token.
   */
  PublicKey getPublicKey(String token) throws InvalidTokenException;
}
