package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.User;

class CouchbaseUserRepository
        extends CouchbaseEntityRepository<User, String> {
    CouchbaseUserRepository() {
        super("users", User.class);
    }
}
