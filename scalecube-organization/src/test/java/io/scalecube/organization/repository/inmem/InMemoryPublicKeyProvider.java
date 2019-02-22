package io.scalecube.organization.repository.inmem;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.scalecube.account.api.OrganizationServiceException;
import io.scalecube.account.api.Token;
import io.scalecube.security.Profile;
import io.scalecube.tokens.InvalidTokenException;
import io.scalecube.tokens.PublicKeyProvider;
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
      KEY_PAIR = KeyPairGenerator.getInstance("RSA").generateKeyPair();
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

  public static Token expiredToken(Profile profile) {
    return token(profile, options -> options.setExpiration(new Date()));
  }
}
