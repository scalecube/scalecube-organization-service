package io.scalecube.organization.repository.exception;

public class AccessPermissionException extends Exception {

  private static final long serialVersionUID = 1L;

  public AccessPermissionException(String message) {
    super(message);
  }

}
