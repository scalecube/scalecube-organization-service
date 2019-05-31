package io.scalecube.organization.fixtures;

import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import reactor.core.publisher.Mono;

public class InMemoryOrganizationRepository extends InMemoryEntityRepository<Organization, String>
    implements OrganizationsRepository {

  @Override
  public Mono<Boolean> existsByName(String name) {
    return existByProperty("name", name);
  }
}
