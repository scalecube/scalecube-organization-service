package io.scalecube.organization.repository.exception;

public class OperationCancellationException extends DataAccessException {
    public OperationCancellationException(String message, Throwable ex) {
        super(message, ex);
    }
}
