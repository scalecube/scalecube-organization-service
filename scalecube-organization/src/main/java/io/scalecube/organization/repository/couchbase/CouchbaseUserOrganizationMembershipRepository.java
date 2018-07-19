package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.lang.reflect.Field;
import java.util.*;

public class CouchbaseUserOrganizationMembershipRepository
        implements UserOrganizationMembershipRepository {

    public static final String ORG_MEMBERS_BUCKET_SUFFIX = "%s-members";

    public CouchbaseUserOrganizationMembershipRepository() {


    }

    @Override
    public void addMemberToOrganization(Organization org, OrganizationMember member) {
        String userInfo = "{}";

//        try {
//            userInfo = toJsonObject(member.user()).toString();
//            JsonObject memberDoc = JsonObject.empty();
//            for (Map.Entry e : toJsonObject(member.user()).entrySet()) {
//                memberDoc.put(e.getKey().toString(), e.getValue());
//            }
//            User u = toUser(memberDoc);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Cluster cluster = CouchbaseCluster.create();//.authenticate("cb_user", "123456");

        try {
            Bucket bucket = cluster.openBucket(getBucketName(org.name()), org.id());
            JsonObject memberDoc = JsonObject.empty()
                    .put("id", member.id())
                    .put("user", toJsonObject(member.user()))
                    .put("role", member.role());
            JsonDocument stored = bucket.upsert(JsonDocument.create(member.id(), memberDoc));
        } finally {
            cluster.disconnect();
        }

    }

    JsonObject toJsonObject(User user) {
        JsonObject map = JsonObject.empty();
        for (Field f :user.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                map.put(f.getName(), f.get(user));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    User toUser(JsonObject info) {
        User user = new User();
        try {
            for (String name : info.getNames()) {
                Field f = user.getClass().getDeclaredField(name);
                f.setAccessible(true);
                f.set(user, info.get(name));
            }
        } catch (Exception e) {e.printStackTrace();}
        return user;
    }

    private String getBucketName(String orgId) {
        return String.format(ORG_MEMBERS_BUCKET_SUFFIX, orgId);
    }

    @Override
    public Set<String> getUserMembership(User user) {
        return null;
    }

    public void _createUserOrganizationMembershipRepository(Organization organization) {
        Cluster cluster = CouchbaseCluster.create().authenticate("Administrator", "123456");
        List<com.couchbase.client.java.cluster.User> users = cluster.clusterManager().getUsers();
        for (com.couchbase.client.java.cluster.User u:users) {
            System.out.println(u);
        }
        cluster.disconnect();
    }

    @Override
    public void createUserOrganizationMembershipRepository(Organization organization) {
        Cluster cluster = CouchbaseCluster.create().authenticate("Administrator", "123456");
        String bucketName = getBucketName(organization.name());

        try {
            cluster.clusterManager().insertBucket(new DefaultBucketSettings.Builder()
                    .type(BucketType.COUCHBASE)
                    .name(bucketName)
                    //.password("s3cret")
                    .quota(100) // megabytes
                    .replicas(0)
                    .indexReplicas(false)
                    .enableFlush(false)
                    .build());
            cluster.clusterManager().upsertUser(bucketName,
                    UserSettings.build()
                            .password(organization.id())
                            .name(bucketName)
                            .roles(Arrays.asList(

                                    // Roles required for the reading of data from
                                    // the bucket.
                                    //
                                    new UserRole("data_reader", bucketName),

                                    // Roles required for the writing of data into
                                    // the bucket.
                                    //
                                    new UserRole("data_writer", bucketName),

                                    // Role required for the creation of indexes
                                    // on the bucket.
                                    //
                                    new UserRole("query_manage_index", bucketName)))

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
