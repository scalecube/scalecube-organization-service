package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import io.scalecube.account.api.Organization;
import java.util.List;
import java.util.Objects;

class CouchbaseOrganizationRepository
    extends CouchbaseEntityRepository<Organization, String> {

  private static final String QUERY_PATTERN = "select count(id) from %s where %s = '%s'";
  private static final String NAME_PROPERTY = "name";

  CouchbaseOrganizationRepository(CouchbaseSettings settings, Cluster cluster) {
    super(settings, cluster, "organizations", Organization.class);
  }

  @Override
  public boolean existByProperty(String propertyName, Object propertyValue) {
    Bucket client = client();
    return execute(() -> isOrganizationNameExists(propertyValue.toString(), client), client);
  }

  private boolean isOrganizationNameExists(String orgName, Bucket bucket) {
    N1qlQuery query = N1qlQuery.simple(
        String.format(QUERY_PATTERN, getBucketName(), NAME_PROPERTY, orgName), null);
    N1qlQueryResult queryResult = bucket.query(query);
    List<N1qlQueryRow> rows = queryResult.allRows();
    return !rows.isEmpty() && Objects.equals(1, rows.get(0).value().get("$1"));
  }
}
