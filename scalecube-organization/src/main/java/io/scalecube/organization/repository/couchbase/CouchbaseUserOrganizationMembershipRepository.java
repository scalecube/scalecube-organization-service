package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.document.RawJsonDocument;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
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
        Cluster cluster = CouchbaseCluster
                .create()
                .authenticate(settings.getCouchbaseAdmin(), settings.getCouchbaseAdminPassword());
        String bucketName = getBucketName(organization.name());

        try {

            cluster.clusterManager().insertBucket(new DefaultBucketSettings.Builder()
                    .type(settings.getOrgMembersBucketType())
                    .name(bucketName)
                    .quota(settings.getOrgMembersBucketQuota()) // megabytes
                    .replicas(settings.getOrgMembersBucketReplicas())
                    .indexReplicas(settings.getOrgMembersBucketIndexReplicas())
                    .enableFlush(settings.getOrgMembersBucketEnableFlush())
                    .build());

            cluster.clusterManager().upsertUser(bucketName,
                    UserSettings.build()
                            .password(organization.id())
                            .name(bucketName)
                            .roles(settings.getOrgMemberUserRoles()
                                            .stream()
                                            .map(role->new UserRole(role, bucketName))
                                            .collect(Collectors.toList())
                                    //Arrays.asList(

                                    // Roles required for the reading of data from
                                    // the bucket.
                                    //
                                    //new UserRole("data_reader", bucketName),

                                    // Roles required for the writing of data into
                                    // the bucket.
                                    //
                                    //new UserRole("data_writer", bucketName)//,

                                    // Role required for the creation of indexes
                                    // on the bucket.
                                    //
                                    //new UserRole("query_manage_index", bucketName)
                                    //)
                            )

            );


        } finally {
            cluster.disconnect();
        }
    }

    @Override
    public Optional<Set<OrganizationMember>> findById(String s) throws EntityNotFoundException {
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

    @Override
    public Iterable<Set<OrganizationMember>> findAllById(Iterable<String> strings) {
        return null;
    }
}
