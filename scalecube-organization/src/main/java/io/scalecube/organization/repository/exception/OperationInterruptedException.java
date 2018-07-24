package io.scalecube.organization.repository.exception;

public class OperationInterruptedException extends DataAccessException {
    public OperationInterruptedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
