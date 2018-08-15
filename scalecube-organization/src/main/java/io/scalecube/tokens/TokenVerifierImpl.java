package io.scalecube.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtAuthenticatorImpl;
import io.scalecube.security.Profile;

import java.security.PublicKey;

import java.util.Objects;
import java.util.Optional;

class TokenVerifierImpl implements TokenVerifier {

  @Override
  public Profile verify(Token token) throws Exception {
    Objects.requireNonNull(token, "token");
    Objects.requireNonNull(token.token(), "token");
    final PublicKey publicKey = getPublicKey(token.token());
    Objects.requireNonNull(publicKey, "Token signing key");
    JwtAuthenticator authenticator = new JwtAuthenticatorImpl
        .Builder()
        .keyResolver(map -> Optional.of(publicKey))
        .build();

    return authenticator.authenticate(token.token());
  }

  private PublicKey getPublicKey(String token) throws Exception {
    return Objects.requireNonNull(
        PublicKeyProviderFactory.getPublicKeyProvider())
        .getPublicKey(token);
  }
}
