package io.scalecube.organization.repository.exception;

public class DataAccessResourceFailureException extends DataAccessException {
    public DataAccessResourceFailureException(String message, RuntimeException ex) {
        super(message, ex);
    }
}
