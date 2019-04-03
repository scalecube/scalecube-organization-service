package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import io.scalecube.organization.operation.Organization;

class CouchbaseOrganizationRepository extends CouchbaseEntityRepository<Organization, String> {

  CouchbaseOrganizationRepository(Bucket bucket) {
    super(bucket, Organization.class);
  }
}
