package io.scalecube.organization.repository.exception;

public class DuplicateKeyException extends Exception {
    public DuplicateKeyException(String key) {
        super(key);
    }
}
