package io.scalecube.tokens.jwk;

public class SigningKeyNotFoundException extends JwkException {

  public SigningKeyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
