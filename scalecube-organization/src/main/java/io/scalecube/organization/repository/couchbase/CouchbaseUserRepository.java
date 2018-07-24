package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.User;

public class CouchbaseUserRepository
        extends CouchbaseEntityRepository<User, String> {
    public CouchbaseUserRepository() {
        super("users", User.class);
    }
}
