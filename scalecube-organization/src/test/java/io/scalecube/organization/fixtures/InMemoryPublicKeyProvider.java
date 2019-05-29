package io.scalecube.organization.fixtures;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.scalecube.account.api.OrganizationServiceException;
import io.scalecube.account.api.Token;
import io.scalecube.organization.tokens.InvalidTokenException;
import io.scalecube.organization.tokens.PublicKeyProvider;
import io.scalecube.security.api.Profile;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Date;
import java.util.function.Consumer;

public class InMemoryPublicKeyProvider implements PublicKeyProvider {

  private static final KeyPair KEY_PAIR;

  static {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KEY_PAIR = keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new OrganizationServiceException("Error during initialing KeyPairGenerator", e);
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
                    .setSubject(profile.userId())
                    .setAudience(profile.tenant())
                    .setHeaderParam("kid", "42")
                    .claim("email", profile.email())
                    .claim("email_verified", profile.isEmailVerified())
                    .claim("name", profile.name())
                    .claim("family_name", profile.familyName())
                    .claim("given_name", profile.givenName())
                    .addClaims(profile.claims())));
  }

  public static Token token(Consumer<JwtBuilder> consumer) {
    JwtBuilder builder = Jwts.builder();
    consumer.accept(builder);
    String token = builder.signWith(SignatureAlgorithm.RS256, KEY_PAIR.getPrivate()).compact();
    return new Token(token);
  }

  public static Token expiredToken(Profile profile) {
    return token(profile, options -> options.setExpiration(new Date()));
  }
}
