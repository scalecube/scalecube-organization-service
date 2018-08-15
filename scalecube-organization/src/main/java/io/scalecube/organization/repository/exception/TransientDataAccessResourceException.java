package io.scalecube.organization.repository.exception;

public class TransientDataAccessResourceException extends DataAccessException {

  public TransientDataAccessResourceException(String message, Throwable ex) {
    super(message, ex);
  }
}
