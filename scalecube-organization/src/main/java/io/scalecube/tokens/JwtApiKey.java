package io.scalecube.tokens;

import io.scalecube.account.api.ApiKey;
import io.scalecube.jwt.WebToken;

import java.util.Map;

/**
 * JSON web token API key.
 */
public class JwtApiKey extends ApiKey {

  /**
   * Constructs an empty JSON web token API key.
   */
  public JwtApiKey() {}

  /**
   * Constructs an empty JSON web token API key with the provided arguments.
   * @param name Key name
   * @param claims Key claims.
   * @param apiKey The API key.
   */
  public JwtApiKey(String name, Map<String, String> claims, String apiKey) {
    super.name = name;
    super.claims = claims;
    super.key = apiKey;
  }

  public static final class Builder {

    private String origin;
    private String subject;
    private Map<String, String> claims;
    private String id;
    private String name;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder origin(String origin) {
      this.origin = origin;
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

    /**
     * Constructs an API key object and signs it using the <code>secret</code> argument.
     * @param secretKey The token signing key.
     * @return an API key.
     */
    public ApiKey build(String secretKey) {
      final WebToken jwt = new WebToken(this.origin, this.subject);
      final String apiKey = jwt.createToken(this.id,
          Long.MAX_VALUE - System.currentTimeMillis(), secretKey, claims);
      return new JwtApiKey(this.name, this.claims, apiKey);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

}