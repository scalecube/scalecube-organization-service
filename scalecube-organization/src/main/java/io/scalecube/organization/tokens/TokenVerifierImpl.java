package io.scalecube.organization.tokens;

import io.scalecube.account.api.Token;
import io.scalecube.security.api.Profile;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
import java.util.Objects;
import reactor.core.publisher.Mono;

public class TokenVerifierImpl implements TokenVerifier {

  private final PublicKeyProvider publicKeyProvider;

  public TokenVerifierImpl(PublicKeyProvider publicKeyProvider) {
    this.publicKeyProvider = publicKeyProvider;
  }

  @Override
  public Mono<Profile> verify(Token token) throws InvalidTokenException {
    return Mono.fromRunnable(
        () -> {
          Objects.requireNonNull(token, "token");
          Objects.requireNonNull(token.token(), "token");
        })
        .then(Mono.fromCallable(() -> publicKeyProvider.getPublicKey(token.token())))
        .doOnNext(publicKey -> Objects.requireNonNull(publicKey, "Token signing key"))
        .map(publicKey -> new DefaultJwtAuthenticator(map -> Mono.just(publicKey)))
        .flatMap(authenticator -> authenticator.authenticate(token.token()))
        .onErrorMap(th -> new InvalidTokenException("Token verification failed", th));
  }
}
