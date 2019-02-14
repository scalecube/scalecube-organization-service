package io.scalecube.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

public class WebToken {

  private final String issuer;
  private final String subject;

  public WebToken(String issuer, String subject) {
    this.issuer = issuer;
    this.subject = subject;
  }

  /**
   * Creates a token using the provided arguments.
   *
   * @param id Token id.
   * @param ttlMillis Token expiration.
   * @param signingKey Signing key.
   * @param claims Token claims.
   * @return A string representation of a token.
   */
  public String createToken(String id, String audience,
      Long ttlMillis, String keyId, Key signingKey,
      Map<String, String> claims) {
    return createWebToken(id, issuer, subject, audience, ttlMillis, keyId, signingKey,
        claims == null ? new HashMap<>() : claims);
  }

  /**
   * Create JWT object.
   *
   * @param id contains id information.
   * @param issuer contains issuer information.
   * @param subject contains subject information.
   * @param ttlMillis contains ttl information.
   * @return returns string if valid.
   */
  private String createWebToken(String id,
      String issuer,
      String subject,
      String audience,
      Long ttlMillis,
      String keyId,
      Key signingKey,
      Map<String, String> claims) {

    // The JWT signature algorithm we will be using to sign the token
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;

    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    // Let's set the JWT Claims
    JwtBuilder builder =
        Jwts.builder()
            .setId(id)
            .setHeaderParam("kid", keyId)
            .setIssuedAt(now)
            .setSubject(subject)
            .setIssuer(issuer)
            .setAudience(audience)
            .signWith(signatureAlgorithm, signingKey);

    for (Map.Entry<String, String> entry : claims.entrySet()) {
      builder.claim(entry.getKey(), entry.getValue());
    }
    // if it has been specified, let's add the expiration
    if (ttlMillis != null && ttlMillis >= 0) {
      long expMillis = nowMillis + ttlMillis;

      if (expMillis > 0) {
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
      }
    }

    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.compact();
  }

  /**
   * Verifies if token is valid.
   *
   * @param token to validate.
   * @param id of the token.
   * @param secretKey this token was encypted with.
   * @return return if valid.
   */
  public boolean isValidToken(String token, String id, String secretKey) {
    Claims claims = parseWebToken(token, secretKey);

    // Make sure id, subject, and issuer are correct
    if (claims != null
        && claims.getId().equals(id)
        && claims.getSubject().equals(subject)
        && claims.getIssuer().equals(issuer)) {
      // Make sure expiration is in the future
      long nowMillis = System.currentTimeMillis();
      Date now = new Date(nowMillis);
      if (claims.getExpiration() != null && claims.getExpiration().after(now)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Parse Web Token object.
   *
   * @param jwt contains jwt information.
   * @return returns if valid.
   */
  private Claims parseWebToken(String jwt, String secretKey) {
    Claims claims = null;
    try {
      // This line will throw an exception if it is not a signed JWS (as expected)
      claims =
          Jwts.parser()
              .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
              .parseClaimsJws(jwt)
              .getBody();
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    return claims;
  }

  public Claims parse(String token, String secretKey) {
    return parseWebToken(token, secretKey);
  }
}
