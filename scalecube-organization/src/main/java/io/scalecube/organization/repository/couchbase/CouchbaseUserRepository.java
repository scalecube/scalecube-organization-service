package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.User;

final class CouchbaseUserRepository
        extends CouchbaseEntityRepository<User, String> {
    CouchbaseUserRepository() {
        super("users", User.class);
    }
}
