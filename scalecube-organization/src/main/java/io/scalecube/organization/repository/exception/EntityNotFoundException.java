package io.scalecube.organization.repository.exception;

public class EntityNotFoundException extends Exception {

  public EntityNotFoundException(String id) {
    super(id);
  }
}
