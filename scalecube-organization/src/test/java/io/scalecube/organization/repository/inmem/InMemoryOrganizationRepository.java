package io.scalecube.organization.repository.inmem;

import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;

public class InMemoryOrganizationRepository extends InMemoryEntityRepository<Organization, String>
    implements OrganizationsRepository {

  @Override
  public boolean existsByName(String name) {
    return existByProperty("name", name);
  }
}
