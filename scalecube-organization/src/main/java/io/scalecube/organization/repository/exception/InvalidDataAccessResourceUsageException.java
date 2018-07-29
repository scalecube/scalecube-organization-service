package io.scalecube.organization.repository.exception;

public class InvalidDataAccessResourceUsageException extends DataAccessException {
    public InvalidDataAccessResourceUsageException(String message, Throwable ex) {
        super(message, ex);
    }
}
