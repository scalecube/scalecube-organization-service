package io.scalecube.organization.repository.exception;

public class QueryTimeoutException extends DataAccessException {

  public QueryTimeoutException(String message, Throwable ex) {
    super(message, ex);
  }
}
