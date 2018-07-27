package io.scalecube.organization.repository.exception;

public class InvalidInputException extends DataAccessException {
    public InvalidInputException(String message) {
        super(message);
    }
}
