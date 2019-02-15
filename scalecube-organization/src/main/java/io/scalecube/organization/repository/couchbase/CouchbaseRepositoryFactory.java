package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import java.util.List;

public final class CouchbaseRepositoryFactory {

  private final CouchbaseSettings settings;
  private final Cluster cluster;
  private final CouchbaseCluster adminCluster;

  /**
   * Creates a couchbase repository factory, initializes two couchbase clusters.
   *
   * @param settings the settings
   */
  public CouchbaseRepositoryFactory(CouchbaseSettings settings) {
    this.settings = settings;
    List<String> nodes = settings.hosts();
    adminCluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);
    adminCluster.authenticate(settings.username(), settings.password());
    cluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);
  }

  public Repository<Organization, String> organizations() {
    return new CouchbaseOrganizationRepository(settings, cluster);
  }

  public UserOrganizationMembershipRepository organizationMembers() {
    return new CouchbaseUserOrganizationMembershipRepository(settings, cluster);
  }

  public CouchbaseOrganizationMembersRepositoryAdmin organizationMembersRepositoryAdmin() {
    return new CouchbaseOrganizationMembersRepositoryAdmin(settings, adminCluster);
  }
}
