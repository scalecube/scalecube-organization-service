package io.scalecube.organization.repository.couchbase;

import static java.util.Objects.requireNonNull;

import com.couchbase.client.java.Bucket;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.repository.OrganizationsRepository;

public class CouchbaseOrganizationsRepository extends CouchbaseRepository<Organization>
    implements OrganizationsRepository {

  CouchbaseOrganizationsRepository(Bucket bucket) {
    super(bucket, Organization.class);
  }

  @Override
  public boolean existsByName(String name) {
    requireNonNull(name, "organization name cannot be null");
    return existByProperty("name", name);
  }
}
