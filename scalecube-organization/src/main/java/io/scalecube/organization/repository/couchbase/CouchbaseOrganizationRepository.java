package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import io.scalecube.account.api.Organization;

import java.util.List;
import java.util.Objects;

class CouchbaseOrganizationRepository
        extends CouchbaseEntityRepository<Organization, String> {

    public static final String QUERY_PATTERN = "select count(id) from %s where %s = '%s'";

    CouchbaseOrganizationRepository() {
        super("organizations", Organization.class);
    }

    @Override
    public boolean existByProperty(String propertyName, Object propertyValue) {
        Bucket client = client();
        return execute(() -> isOrganizationNameExists(propertyValue.toString(), client),client);
    }

    private boolean isOrganizationNameExists(String orgName, Bucket client) {
        N1qlQuery query = N1qlQuery.simple(
                String.format(QUERY_PATTERN, getBucketName(), "name", orgName), null);
        N1qlQueryResult queryResult = client.query(query);
        List<N1qlQueryRow> rows = queryResult.allRows();
        boolean exists = !rows.isEmpty() && Objects.equals(1, rows.get(0).value().get("$1"));
        return exists;
    }
}
