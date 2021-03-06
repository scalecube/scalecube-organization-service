package io.scalecube.organization.tokens;

import io.scalecube.account.api.ApiKey;
import io.scalecube.organization.jwt.WebToken;
import java.security.Key;
import java.util.Map;

/** JSON web token API key. */
public class JwtApiKey extends ApiKey {

  /** Constructs an empty JSON web token API key. */
  public JwtApiKey() {}

  /**
   * Constructs an empty JSON web token API key with the provided arguments.
   *
   * @param name Key name
   * @param claims Key claims.
   * @param apiKey The API key.
   */
  public JwtApiKey(String name, Map<String, String> claims, String apiKey, String keyId) {
    super.name = name;
    super.claims = claims;
    super.key = apiKey;
    super.keyId = keyId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String issuer;
    private String subject;
    private Map<String, String> claims;
    private String id;
    private String name;
    private Long tokenTimeToLiveInMillis;
    private String audience;
    private String keyId;
    private Key signingKey;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder issuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder claims(Map<String, String> claims) {
      this.claims = claims;
      return this;
    }

    public Builder expiration(Long tokenTimeToLiveInMillis) {
      this.tokenTimeToLiveInMillis = tokenTimeToLiveInMillis;
      return this;
    }

    public Builder keyId(String keyId) {
      this.keyId = keyId;
      return this;
    }

    public Builder signingKey(Key signingKey) {
      this.signingKey = signingKey;
      return this;
    }

    public Builder audience(String audience) {
      this.audience = audience;
      return this;
    }

    /**
     * Constructs an API key object and signs it using the <code>secret</code> argument.
     *
     * @return an API key.
     */
    public ApiKey build() {
      final WebToken jwt = new WebToken(this.issuer, this.subject);
      final String apiKey =
          jwt.createToken(
              this.id,
              this.audience,
              this.tokenTimeToLiveInMillis,
              this.keyId,
              this.signingKey,
              claims);
      return new JwtApiKey(this.name, this.claims, apiKey, keyId);
    }
  }
}
