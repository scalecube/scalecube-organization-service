package io.scalecube.organization.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.api.Profile;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
import io.scalecube.security.jwt.JwtAuthenticator;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Objects;

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
      JwtAuthenticator authenticator = new DefaultJwtAuthenticator(map -> publicKey);

      return authenticator.authenticate(token.token()).block(Duration.ofSeconds(10)); // todo
    } catch (Exception e) {
      throw new InvalidTokenException("Token verification failed", e);
    }
  }
}
