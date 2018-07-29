package io.scalecube.organization.repository.exception;

public class DuplicateKeyException extends DataAccessException {
    public DuplicateKeyException(String key) {
        super(key);
    }

    public DuplicateKeyException(String message, Throwable ex) {
        super(message, ex);
    }
}
