package io.scalecube.organization.repository;

import io.scalecube.organization.domain.Organization;
import reactor.core.publisher.Mono;

public interface OrganizationsRepository extends Repository<Organization, String> {

  /**
   * Returns whether an organization with the given name exists.
   *
   * @param name organization name.
   * @return {@code true} if an entity with the given id exists, {@code false} otherwise.
   */
  Mono<Boolean> existsByName(String name);
}
