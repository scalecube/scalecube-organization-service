package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.*;
import com.couchbase.client.java.document.RawJsonDocument;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import java.util.*;
import java.util.stream.Collectors;

public class CouchbaseUserOrganizationMembershipRepository
        implements UserOrganizationMembershipRepository {

    final CouchbaseSettings settings = new CouchbaseSettings();

    @Override
    public void addMemberToOrganization(Organization org, OrganizationMember member) {
        Cluster cluster = CouchbaseCluster.create();
        JacksonTranslationService service = new JacksonTranslationService();

        try {
            Bucket bucket = cluster.openBucket(getBucketName(org.name()), org.id());
            RawJsonDocument d = RawJsonDocument.create(member.id(), service.encode(member));
            bucket.insert(d);
        } finally {
            cluster.disconnect();
        }

    }

    private String getBucketName(String orgId) {
        return String.format(settings.getOrgMembersBucketSuffix(), orgId);
    }

    @Override
    public Set<String> getUserMembership(User user) {
        return null;
    }

    @Override
    public void createUserOrganizationMembershipRepository(Organization organization) {
        List<String> nodes = settings.getCouchbaseClusterNodes();
        String bucketName = getBucketName(organization.name());

        Cluster cluster = nodes.isEmpty()
                ? CouchbaseCluster.create()
                : CouchbaseCluster.create(nodes);

        cluster.authenticate(settings.getCouchbaseAdmin(), settings.getCouchbaseAdminPassword());

        try {
            insertBucket(bucketName, cluster);
            upsertUser(organization, bucketName, cluster);
        } finally {
            cluster.disconnect();
        }
    }

    private void upsertUser(Organization organization, String bucketName, Cluster cluster) {
        cluster.clusterManager().upsertUser(AuthDomain.LOCAL,
                bucketName,
                UserSettings.build()
                        .password(organization.id())
                        .name(bucketName)
                        .roles(settings.getOrgMemberUserRoles()
                                .stream()
                                .map(role -> new UserRole(role, bucketName))
                                .collect(Collectors.toList())));
    }

    private void insertBucket(String bucketName, Cluster cluster) {
        cluster.clusterManager().insertBucket(new DefaultBucketSettings.Builder()
                .type(settings.getOrgMembersBucketType())
                .name(bucketName)
                .quota(settings.getOrgMembersBucketQuota()) // megabytes
                .replicas(settings.getOrgMembersBucketReplicas())
                .indexReplicas(settings.getOrgMembersBucketIndexReplicas())
                .enableFlush(settings.getOrgMembersBucketEnableFlush())
                .build());
    }

    @Override
    public Optional<Set<OrganizationMember>> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public Set<OrganizationMember> save(String s, Set<OrganizationMember> organizationMembers) {
        return null;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public Iterable<Set<OrganizationMember>> findAll() {
        return null;
    }

}
