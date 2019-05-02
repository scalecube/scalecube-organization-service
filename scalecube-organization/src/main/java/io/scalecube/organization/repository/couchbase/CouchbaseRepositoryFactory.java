package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.organization.repository.OrganizationsRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CouchbaseRepositoryFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseRepositoryFactory.class);

  private Bucket bucket;

  /**
   * Creates a couchbase repository factory, initializes two couchbase clusters.
   *
   * @param settings the settings
   */
  public CouchbaseRepositoryFactory(CouchbaseSettings settings) {
    List<String> nodes = settings.hosts();

    Cluster cluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);

    int time = 0;
    int maxTimes = 3;
    do {
      try {
        bucket = cluster
            .authenticate(settings.username(), settings.password())
            .openBucket(settings.organizationsBucketName());
        break;
      } catch (Exception e) {
        if (++time == maxTimes) {
          LOGGER.warn(e.getMessage() + " [times: " + time + "]", e);
          throw e;
        }
      }
    } while (true);
  }

  public OrganizationsRepository organizations() {
    return new CouchbaseOrganizationsRepository(bucket);
  }
}
