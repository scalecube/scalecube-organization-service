package io.scalecube.organization.tokens;

/**
 * Represents an exception that gets thrown during an attempt to extract and retreive a Public Key
 * that was used to sign a JWT access token.
 */
public class PublicKeyProviderException extends RuntimeException {

  public PublicKeyProviderException(Throwable cause) {
    this(null, cause);
  }

  public PublicKeyProviderException(String message) {
    this(message, null);
  }

  public PublicKeyProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
