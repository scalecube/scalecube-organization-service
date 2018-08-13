package io.scalecube.tokens;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.scalecube.config.AppConfiguration;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Objects;

/**
 * Extracts the public key ID of the key used to sigh the JWT token from the token's header 'kid'
 * claim.
 * This class utilize the auth0 API to retrieve the JSON web key corresponding to the
 * kid token header claim value.
 * The JWK provider URL is read from configuration file and is the only dependency this class
 * requires.
 */
public class Auth0PublicKeyProvider implements PublicKeyProvider {

  private static final String MISSING_JWK_PROVIDER_URL = "Missing JWK provider URL";
  private static final String FAILED_TO_DECODE_TOKEN = "Failed to decode token";
  private static final String MISSING_KEY_ID_CLAIM_IN_TOKEN_HEADER
      = "Missing key id claim in token header.";
  private static final String KID_CLAIM_NAME = "kid";
  private static final String FAILED_TO_GET_KEY_FROM_JWK_PROVIDER
      = "Failed to get key from JWK provider using kid=%s";
  private static final String FAILED_TO_GET_PUBLIC_KEY = "Failed to get public key.";
  private final String domain;
  private final HashMap<String, PublicKey> cacahe = new HashMap<>();

  /**
   * Constructs an instance of Auth0PublicKeyProvider. PublicKeyProvider instantiates this class
   * using reflection.
   */
  public Auth0PublicKeyProvider() {
    domain = AppConfiguration.builder().build().getProperty("url.jwk.provider");
    Objects.requireNonNull(domain, MISSING_JWK_PROVIDER_URL);

    if (domain.length() == 0) {
      throw new PublicKeyProviderException(MISSING_JWK_PROVIDER_URL);
    }
  }

  @Override
  public PublicKey getPublicKey(String token) throws InvalidTokenException {
    Objects.requireNonNull(token, "token");
    DecodedJWT jwt = decode(token);
    String kid = kid(jwt);
    Objects.requireNonNull(kid, "public key id");

    if (!cacahe.containsKey(kid)) {
      Jwk jwk = jwk(domain, kid);
      cacahe.put(kid, key(jwk));
    }

    return cacahe.get(kid);
  }

  private DecodedJWT decode(String token) throws InvalidTokenException {
    try {
      return JWT.decode(token);
    } catch (JWTDecodeException ex) {
      throw new InvalidTokenException(FAILED_TO_DECODE_TOKEN, ex);
    }
  }

  private String kid(DecodedJWT jwt) throws InvalidTokenException {
    Claim kid = jwt.getHeaderClaim(KID_CLAIM_NAME);

    if (kid.isNull()) {
      throw new InvalidTokenException(MISSING_KEY_ID_CLAIM_IN_TOKEN_HEADER);
    }

    return kid.asString();
  }

  private Jwk jwk(String domain, String kid) throws InvalidTokenException {
    JwkProvider provider = new UrlJwkProvider(domain);

    try {
      return provider.get(kid);
    } catch (JwkException ex) {
      throw new InvalidTokenException(String.format(FAILED_TO_GET_KEY_FROM_JWK_PROVIDER, kid), ex);
    }
  }

  private PublicKey key(Jwk jwk) throws InvalidTokenException {
    try {
      return jwk.getPublicKey();
    } catch (InvalidPublicKeyException ex) {
      throw new InvalidTokenException(FAILED_TO_GET_PUBLIC_KEY, ex);
    }
  }
}
