package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.Organization;

public class CouchbaseOrganizationRepository
        extends CouchbaseEntityRepository<Organization, String> {

    public CouchbaseOrganizationRepository(OrganizationRepository repository) {
        super(repository);
    }
}
