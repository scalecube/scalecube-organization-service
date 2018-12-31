package io.scalecube.account.api;

/**
 * Represents an access token.
 */
public class Token {

  private String token;

  public Token() {}

  /**
   * Token information.
   * 
   * @param token the jwt token string.
   */
  public Token(String token) {
    this.token = token;
  }

  public String token() {
    return this.token;
  }
  
  @Override
  public String toString() {
    return "Token [" + token + "]";
  }
}
