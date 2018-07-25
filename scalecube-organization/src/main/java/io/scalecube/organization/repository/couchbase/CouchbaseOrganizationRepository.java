package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.Organization;

class CouchbaseOrganizationRepository
        extends CouchbaseEntityRepository<Organization, String> {
    CouchbaseOrganizationRepository() {
        super("organizations", Organization.class);
    }
}
