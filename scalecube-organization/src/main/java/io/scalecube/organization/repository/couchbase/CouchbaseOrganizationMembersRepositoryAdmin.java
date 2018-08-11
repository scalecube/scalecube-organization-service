package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.AuthDomain;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

import io.scalecube.account.api.Organization;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.exception.CreatePrimaryIndexException;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

final class CouchbaseOrganizationMembersRepositoryAdmin implements
    OrganizationMembersRepositoryAdmin {

  private static final String CREATE_PRIMARY_INDEX
      = "CREATE PRIMARY INDEX `%s-primary-idx` ON `%s`";

  private final CouchbaseSettings settings;

  private CouchbaseOrganizationMembersRepositoryAdmin(CouchbaseSettings settings) {
    this.settings = settings;
  }

  static Builder builder() {
    return new Builder();
  }

  @Override
  public void createRepository(Organization organization) {
    String bucketName = getBucketName(organization.name());
    cluster(cluster -> {
      cluster.clusterManager().insertBucket(new DefaultBucketSettings.Builder()
          .type(settings.getOrgMembersBucketType())
          .name(bucketName)
          .quota(settings.getOrgMembersBucketQuota()) // megabytes
          .replicas(settings.getOrgMembersBucketReplicas())
          .indexReplicas(settings.getOrgMembersBucketIndexReplicas())
          .enableFlush(settings.getOrgMembersBucketEnableFlush())
          .build());

      try {
        createPrimaryIndex(bucketName, cluster);
        insertUser(organization.id(), bucketName);
      } catch (Throwable throwable) {
        // rollback
        cluster.clusterManager().removeBucket(bucketName);
        throw throwable;
      }
    });

  }

  private void createPrimaryIndex(String bucketName, Cluster cluster) {
    N1qlQuery index = N1qlQuery.simple(String.format(CREATE_PRIMARY_INDEX, bucketName, bucketName));
    N1qlQueryResult queryResult = cluster.openBucket(bucketName).query(index);

    if (!queryResult.finalSuccess()) {
      StringBuilder buffer = new StringBuilder();
      for (JsonObject error : queryResult.errors()) {
        buffer.append(error);
      }
      throw new CreatePrimaryIndexException(buffer.toString());
    }
  }

  private void insertUser(String password, String bucketName) {
    cluster(cluster -> cluster.clusterManager().upsertUser(AuthDomain.LOCAL,
        bucketName,
        UserSettings.build()
            .password(password)
            .name(bucketName)
            .roles(settings.getOrgMemberUserRoles()
                .stream()
                .map(role -> new UserRole(role, bucketName))
                .collect(Collectors.toList()))));
  }


  private String getBucketName(String orgId) {
    return String.format(settings.getOrgMembersBucketSuffix(), orgId);
  }

  @Override
  public void deleteRepository(Organization organization) {
    String bucketName = getBucketName(organization.name());
    cluster(cluster -> cluster.clusterManager().removeUser(AuthDomain.LOCAL, bucketName));
    cluster(cluster -> cluster.clusterManager().removeBucket(bucketName));
  }

  private void cluster(Consumer<Cluster> clusterConsumer) {
    List<String> nodes = settings.getCouchbaseClusterNodes();

    Cluster cluster = nodes.isEmpty()
        ? CouchbaseCluster.create()
        : CouchbaseCluster.create(nodes);

    cluster.authenticate(settings.getCouchbaseAdmin(), settings.getCouchbaseAdminPassword());

    try {
      clusterConsumer.accept(cluster);
    } finally {
      cluster.disconnect();
    }
  }

  public static class Builder {
    public CouchbaseOrganizationMembersRepositoryAdmin build() {
      return new CouchbaseOrganizationMembersRepositoryAdmin(
          new CouchbaseSettings.Builder().build());
    }
  }
}
