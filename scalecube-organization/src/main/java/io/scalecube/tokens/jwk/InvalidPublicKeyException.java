package io.scalecube.tokens.jwk;

public class InvalidPublicKeyException extends JwkException {

  public InvalidPublicKeyException(String msg, Throwable cause) {
    super(msg, cause);
  }
}