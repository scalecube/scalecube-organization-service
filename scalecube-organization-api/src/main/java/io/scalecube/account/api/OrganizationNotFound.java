package io.scalecube.account.api;

public class OrganizationNotFound extends Throwable {

  private static final long serialVersionUID = 1L;

  public OrganizationNotFound(String organizationId) {
    super("organization was found id:[" + organizationId + "]");
  }

}
