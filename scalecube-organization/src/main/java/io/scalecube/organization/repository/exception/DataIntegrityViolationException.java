package io.scalecube.organization.repository.exception;

public class DataIntegrityViolationException extends DataAccessException {

  public DataIntegrityViolationException(String message, Throwable ex) {
    super(message, ex);
  }
}
