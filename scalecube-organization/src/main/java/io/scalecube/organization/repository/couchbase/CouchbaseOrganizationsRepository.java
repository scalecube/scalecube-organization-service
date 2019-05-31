package io.scalecube.organization.repository.couchbase;

import static java.util.Objects.requireNonNull;

import com.couchbase.client.java.AsyncBucket;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;
import reactor.core.publisher.Mono;

public class CouchbaseOrganizationsRepository extends CouchbaseRepository<Organization>
    implements OrganizationsRepository {

  public CouchbaseOrganizationsRepository(AsyncBucket bucket) {
    super(bucket, Organization.class);
  }

  @Override
  public Mono<Boolean> existsByName(String name) {
    requireNonNull(name, "organization name cannot be null");
    return existByProperty("name", name);
  }
}
