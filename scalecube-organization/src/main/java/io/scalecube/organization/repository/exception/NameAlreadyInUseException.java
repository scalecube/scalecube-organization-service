package io.scalecube.organization.repository.exception;

public class NameAlreadyInUseException extends DataAccessException {

  public NameAlreadyInUseException(String message) {
    super(message);
  }
}
