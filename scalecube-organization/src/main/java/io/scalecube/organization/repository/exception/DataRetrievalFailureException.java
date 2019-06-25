package io.scalecube.organization.repository.exception;

public class DataRetrievalFailureException extends DataAccessException {

  public DataRetrievalFailureException(String message) {
    super(message);
  }

  public DataRetrievalFailureException(String message, Throwable ex) {
    super(message, ex);
  }
}
