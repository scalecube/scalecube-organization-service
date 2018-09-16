package io.scalecube.account.api;

/**
 * Represents an access token.
 */
public class Token {

  private String issuer;
  private String token;

  public Token() {}

  /**
   * Token information.
   * 
   * @param issuer where this token was provided.
   * @param token the jwt token string.
   */
  public Token(String issuer, String token) {
    this.token = token;
    this.issuer = issuer;
  }

  public String token() {
    return this.token;
  }

  /**
   * source for this token for example: google, twitter, github.
   * 
   * @return issuer of the token.
   */
  public String issuer() {
    return this.issuer;
  }

  @Override
  public String toString() {
    return "Token [issuer=" + issuer + ", token=" + token + "]";
  }
}
