package io.scalecube.organization.tokens;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Objects;

/**
 * Extracts the public get ID of the get used to sigh the JWT token from the token's header <code>
 * kid</code> claim.
 *
 * <p>This class utilize the auth0 API to retrieve the JSON web public key corresponding to the
 * <code>kid</code> token header claim value. The JWK provider URL is the token's issuer claim
 * value.
 */
public class Auth0PublicKeyProvider implements PublicKeyProvider {

  private static final String TOKEN_BODY_CLAIM_ISSUER = "token body claim: 'issuer'";
  private static final String FAILED_TO_PARSE_TOKEN = "Failed to parse token";
  private static final String MISSING_KEY_ID_CLAIM_IN_TOKEN_HEADER =
      "Token header claim: 'kid' not found.";
  private static final String KID_CLAIM_NAME = "kid";
  private static final String FAILED_TO_GET_KEY_FROM_JWK_PROVIDER =
      "Failed to get public key from JWK provider using kid=%s";
  private static final String FAILED_TO_GET_PUBLIC_KEY = "Failed to get public key.";
  private final HashMap<String, PublicKey> cache = new HashMap<>();

  @Override
  public PublicKey getPublicKey(String token) throws InvalidTokenException {
    Objects.requireNonNull(token, "token");
    String tokenWithoutSignature = TokenUtils.removeSignature(token);
    Jwt<Header, Claims> jwt = parse(tokenWithoutSignature);
    String kid = getKeyId(jwt);
    String issuer = jwt.getBody().getIssuer();

    cache.computeIfAbsent(kid, key -> get(issuer, kid));

    return cache.get(kid);
  }

  private Jwt<Header, Claims> parse(String token) {
    try {
      return Jwts.parser().parseClaimsJwt(token);
    } catch (Exception ex) {
      throw new InvalidTokenException(FAILED_TO_PARSE_TOKEN, ex);
    }
  }

  private String getKeyId(Jwt<Header, Claims> jwt) {
    Object kid = jwt.getHeader().get(KID_CLAIM_NAME);

    if (kid == null || kid.toString().length() == 0) {
      throw new InvalidTokenException(MISSING_KEY_ID_CLAIM_IN_TOKEN_HEADER);
    }

    return kid.toString();
  }

  private PublicKey get(String issuer, String kid) {
    Objects.requireNonNull(issuer, TOKEN_BODY_CLAIM_ISSUER);
    Jwk jwk = getJwkProvider(issuer, kid);
    try {
      return jwk.getPublicKey();
    } catch (InvalidPublicKeyException ex) {
      throw new InvalidTokenException(FAILED_TO_GET_PUBLIC_KEY, ex);
    }
  }

  private Jwk getJwkProvider(String issuer, String kid) {
    JwkProvider provider = new UrlJwkProvider(issuer);
    try {
      return provider.get(kid);
    } catch (JwkException ex) {
      throw new InvalidTokenException(String.format(FAILED_TO_GET_KEY_FROM_JWK_PROVIDER, kid), ex);
    }
  }
}
