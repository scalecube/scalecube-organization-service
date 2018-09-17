package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.account.api.Organization;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import java.util.List;

public final class CouchbaseRepositoryFactory {

  private final CouchbaseSettings settings;
  private final Cluster cluster;
  private final CouchbaseCluster adminCluster;

  public CouchbaseRepositoryFactory(CouchbaseSettings settings) {
    this.settings = settings;
    List<String> nodes = settings.getCouchbaseClusterNodes();
    adminCluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);
    adminCluster.authenticate(settings.getCouchbaseUsername(), settings.getCouchbasePassword());
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
