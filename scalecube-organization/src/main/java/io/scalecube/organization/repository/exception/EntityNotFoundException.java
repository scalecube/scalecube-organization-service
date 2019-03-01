package io.scalecube.organization.repository.exception;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(String id) {
    super(id);
  }
}
