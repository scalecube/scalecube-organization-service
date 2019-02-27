package io.scalecube.account.api;

public class InvalidRequestException extends OrganizationServiceException {

  public InvalidRequestException(String message) {
    super(message);
  }
}
