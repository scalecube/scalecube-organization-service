package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.organization.operation.Organization;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import java.util.List;

public final class CouchbaseRepositoryFactory {

  private final Bucket bucket;

  /**
   * Creates a couchbase repository factory, initializes two couchbase clusters.
   *
   * @param settings the settings
   */
  public CouchbaseRepositoryFactory(CouchbaseSettings settings) {
    List<String> nodes = settings.hosts();

    Cluster cluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);

    bucket =
        cluster
            .authenticate(settings.username(), settings.password())
            .openBucket(settings.organizationsBucketName());
  }

  public Repository<Organization, String> organizations() {
    return new CouchbaseOrganizationRepository(bucket);
  }

  public UserOrganizationMembershipRepository organizationMembers() {
    return new CouchbaseUserOrganizationMembershipRepository(bucket);
  }
}
