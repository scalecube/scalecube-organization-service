package io.scalecube.organization.repository.couchbase;

import io.scalecube.account.api.Organization;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

class CouchbaseOrganizationRepositoryTest {

    @org.junit.jupiter.api.Test
    void save() {
        CouchbaseOrganizationRepository r = new CouchbaseOrganizationRepository();
        Organization o = Organization.builder().name("myorg").build();
        r.save(o.id(), o);
        Organization o2 = r.findById(o.id()).orElse(null);
        assertThat("organization", o2, is(notNullValue()));
        assertTrue(r.existsById(o.id()));
        Iterable<Organization> all = r.findAll();
        List<Organization> list = StreamSupport.stream(all.spliterator(), false)
                .collect(Collectors.toList());
        r.deleteById(o2.id());
    }
}