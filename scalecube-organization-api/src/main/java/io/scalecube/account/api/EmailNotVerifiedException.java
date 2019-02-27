package io.scalecube.account.api;

public class EmailNotVerifiedException extends OrganizationServiceException {

  public EmailNotVerifiedException(String message) {
    super(message);
  }
}
