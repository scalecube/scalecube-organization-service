package io.scalecube.account.api;

/**
 * The {@link ServiceOperationException} represents an error that can be thrown during execution
 * of a service request.
 */
public class ServiceOperationException extends Throwable {
  public ServiceOperationException(String message, Throwable cause) {
    super(message,cause);
  }

  public ServiceOperationException(String message) {
    super(message);
  }
}
