package io.scalecube.organization.opearation;

import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.security.Profile;

public class OperationServiceContext {
  private final Profile profile;
  private final OrganizationsDataAccess repository;

  public OperationServiceContext(Profile profile,
      OrganizationsDataAccess repository) {
    this.profile = profile;
    this.repository = repository;
  }

  public Profile profile() {
    return profile;
  }

  public OrganizationsDataAccess repository() {
    return repository;
  }
}
