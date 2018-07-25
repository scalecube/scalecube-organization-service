package io.scalecube.organization.repository.exception;

public class DataRetrievalFailureException extends DataAccessException {
    public DataRetrievalFailureException(String message) {
        super(message);
    }

    public DataRetrievalFailureException(String message, RuntimeException ex) {
        super(message, ex);
    }
}
