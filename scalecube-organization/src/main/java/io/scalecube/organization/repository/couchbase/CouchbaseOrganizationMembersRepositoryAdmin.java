package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.AuthDomain;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.query.Index;
import io.scalecube.account.api.Organization;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class CouchbaseOrganizationMembersRepositoryAdmin implements OrganizationMembersRepositoryAdmin {
    private final CouchbaseSettings settings;

    private CouchbaseOrganizationMembersRepositoryAdmin(CouchbaseSettings settings) {
        this.settings = settings;
    }


    @Override
    public void createRepository(Organization organization) {
        String bucketName = getBucketName(organization.name());
        cluster(c -> c.clusterManager().insertBucket(new DefaultBucketSettings.Builder()
                .type(settings.getOrgMembersBucketType())
                .name(bucketName)
                .quota(settings.getOrgMembersBucketQuota()) // megabytes
                .replicas(settings.getOrgMembersBucketReplicas())
                .indexReplicas(settings.getOrgMembersBucketIndexReplicas())
                .enableFlush(settings.getOrgMembersBucketEnableFlush())
                .build()));
        Index.createNamedPrimaryIndex(bucketName + "-primary-index").on(bucketName);
        insertUser(organization.id(), bucketName);
    }

    @Override
    public void deleteRepository(Organization organization) {
        String bucketName = getBucketName(organization.name());
        cluster(c -> c.clusterManager().removeUser(AuthDomain.LOCAL, bucketName));
        cluster(c -> c.clusterManager().removeBucket(bucketName));
    }


    private void insertUser(String password, String bucketName) {
        cluster(c -> c.clusterManager().upsertUser(AuthDomain.LOCAL,
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

    private void cluster(Consumer<Cluster> c) {
        List<String> nodes = settings.getCouchbaseClusterNodes();

        Cluster cluster = nodes.isEmpty()
                ? CouchbaseCluster.create()
                : CouchbaseCluster.create(nodes);

        cluster.authenticate(settings.getCouchbaseAdmin(), settings.getCouchbaseAdminPassword());

        try {
            c.accept(cluster);
        } finally {
            cluster.disconnect();
        }
    }

    public static class Builder {
        public CouchbaseOrganizationMembersRepositoryAdmin build() {
            return new CouchbaseOrganizationMembersRepositoryAdmin(new CouchbaseSettings.Builder().build());
        }
    }
}
