package io.scalecube.organization.repository.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String id) {
        super(id);
    }
}
