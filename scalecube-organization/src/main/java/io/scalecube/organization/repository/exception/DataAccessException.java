package io.scalecube.organization.repository.exception;

public abstract class DataAccessException extends RuntimeException {
    public DataAccessException(String msg) {
        super(msg);
    }

    public DataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
