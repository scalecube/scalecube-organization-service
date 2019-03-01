package io.scalecube.account.api;

public class OrganizationNotFoundException extends OrganizationServiceException {

  public OrganizationNotFoundException(String organizationId) {
    super("Organization [id=" + organizationId + "] not found");
  }
}
