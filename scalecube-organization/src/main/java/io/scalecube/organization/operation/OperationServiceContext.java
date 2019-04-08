package io.scalecube.organization.operation;

import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.security.Profile;

public class OperationServiceContext {
  private final Profile profile;
  private final OrganizationsRepository repository;

  public OperationServiceContext(Profile profile, OrganizationsRepository repository) {
    this.profile = profile;
    this.repository = repository;
  }

  public Profile profile() {
    return profile;
  }

  public OrganizationsRepository repository() {
    return repository;
  }
}
