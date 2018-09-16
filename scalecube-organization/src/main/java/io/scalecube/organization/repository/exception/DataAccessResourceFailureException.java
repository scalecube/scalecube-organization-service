package io.scalecube.organization.repository.exception;

public class DataAccessResourceFailureException extends DataAccessException {

  public DataAccessResourceFailureException(String message, Throwable ex) {
    super(message, ex);
  }
}
