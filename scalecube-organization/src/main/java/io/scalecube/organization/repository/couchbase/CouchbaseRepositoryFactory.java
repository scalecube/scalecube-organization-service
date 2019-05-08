package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment.Builder;
import io.scalecube.organization.repository.OrganizationsRepository;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;

public final class CouchbaseRepositoryFactory {

  private Bucket bucket;

  /**
   * Creates a couchbase repository factory, initializes two couchbase clusters.
   *
   * @param settings the settings
   */
  public CouchbaseRepositoryFactory(CouchbaseSettings settings) {
    List<String> nodes = settings.hosts();

    DefaultCouchbaseEnvironment env = new Builder()
        .managementTimeout(TimeUnit.SECONDS.toMillis(15))
        .queryTimeout(TimeUnit.SECONDS.toMillis(15))
        .viewTimeout(TimeUnit.SECONDS.toMillis(15))
        .searchTimeout(TimeUnit.SECONDS.toMillis(15))
        .analyticsTimeout(TimeUnit.SECONDS.toMillis(15))
        .build();

    Cluster cluster =
        nodes.isEmpty() ? CouchbaseCluster.create(env) : CouchbaseCluster.create(env, nodes);

    bucket = Mono.fromCallable(() ->
        cluster
            .authenticate(settings.username(), settings.password())
            .openBucket(settings.organizationsBucketName())).retryBackoff(3, Duration.ofSeconds(1))
        .block(Duration.ofSeconds(30));
  }

  public OrganizationsRepository organizations() {
    return new CouchbaseOrganizationsRepository(bucket);
  }
}
