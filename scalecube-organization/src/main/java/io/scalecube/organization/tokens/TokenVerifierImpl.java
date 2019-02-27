package io.scalecube.organization.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtAuthenticatorImpl;
import io.scalecube.security.Profile;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Optional;

public class TokenVerifierImpl implements TokenVerifier {

  private final PublicKeyProvider publicKeyProvider;

  public TokenVerifierImpl(PublicKeyProvider publicKeyProvider) {
    this.publicKeyProvider = publicKeyProvider;
  }

  @Override
  public Profile verify(Token token) throws InvalidTokenException {
    try {
      Objects.requireNonNull(token, "token");
      Objects.requireNonNull(token.token(), "token");
      final PublicKey publicKey = publicKeyProvider.getPublicKey(token.token());
      Objects.requireNonNull(publicKey, "Token signing key");
      JwtAuthenticator authenticator =
          new JwtAuthenticatorImpl.Builder().keyResolver(map -> Optional.of(publicKey)).build();

      return authenticator.authenticate(token.token());
    } catch (Exception e) {
      throw new InvalidTokenException("Token verification failed", e);
    }
  }
}
