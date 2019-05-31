package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.organization.repository.OrganizationsRepository;
import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Mono;

public final class CouchbaseRepositoryFactory {

  private AsyncBucket bucket;

  /**
   * Creates a couchbase repository factory, initializes two couchbase clusters.
   *
   * @param settings the settings
   */
  public CouchbaseRepositoryFactory(CouchbaseSettings settings) {
    List<String> nodes = settings.hosts();

    Cluster cluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);

    bucket =
        Mono.fromCallable(
            () ->
                cluster
                    .authenticate(settings.username(), settings.password())
                    .openBucket(settings.organizationsBucketName())
                    .async())
            .retryBackoff(3, Duration.ofSeconds(1))
            .block(Duration.ofSeconds(30));
  }

  public OrganizationsRepository organizations() {
    return new CouchbaseOrganizationsRepository(bucket);
  }
}
