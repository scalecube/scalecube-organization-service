package io.scalecube.organization;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;
import io.scalecube.tokens.InvalidTokenException;
import io.scalecube.tokens.PublicKeyProvider;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.function.Consumer;
import reactor.core.Exceptions;

public class MockPublicKeyProvider implements PublicKeyProvider {

  private static final KeyPairGenerator KPG;
  private static final KeyPair KEY_PAIR;

  static {
    try {
      KPG = KeyPairGenerator.getInstance("RSA");
      KPG.initialize(2048);
      KEY_PAIR = KPG.generateKeyPair();
    } catch (Exception e) {
      throw Exceptions.propagate(e);
    }
  }

  @Override
  public PublicKey getPublicKey(String token) throws InvalidTokenException {
    return KEY_PAIR.getPublic();
  }

  public static Token token(Profile profile) {
    return token(profile, options -> {});
  }

  public static Token token(Profile profile, Consumer<JwtBuilder> consumer) {
    return token(
        options ->
            consumer.accept(
                options
                    .setSubject(profile.getUserId())
                    .setAudience(profile.getTenant())
                    .setHeaderParam("kid", "42")
                    .claim("email", profile.getEmail())
                    .claim("email_verified", profile.isEmailVerified())
                    .claim("name", profile.getName())
                    .claim("family_name", profile.getFamilyName())
                    .claim("given_name", profile.getGivenName())
                    .addClaims(profile.getClaims())));
  }

  public static Token token(Consumer<JwtBuilder> consumer) {
    JwtBuilder builder = Jwts.builder();
    consumer.accept(builder);
    String token = builder.signWith(SignatureAlgorithm.RS256, KEY_PAIR.getPrivate()).compact();
    return new Token(token);
  }
}
