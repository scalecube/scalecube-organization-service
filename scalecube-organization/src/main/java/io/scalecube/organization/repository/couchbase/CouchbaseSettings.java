package io.scalecube.organization.repository.couchbase;
/*
# org members quota in megabytes
org.members.bucket.quota=100
org.members.bucket.replicas=0
org.members.bucket.indexReplicas=false
org.members.bucket.enableFlush=false
 */
import com.couchbase.client.java.bucket.BucketType;
import java.io.IOException;
import java.util.*;

final class CouchbaseSettings {
    private static final String COUCHBASE_ADMIN = "couchbase.admin.user";
    private static final String COUCHBASE_ADMIN_PASSWORD = "couchbase.admin.password";
    private static final String COUCHBASE_CLUSTER_NODES = "couchbase.cluster.nodes";
    private static final String ORG_MEMBERS_BUCKET_SUFFIX = "org.members.bucket.suffix";
    private static final String ORG_MEMBERS_USER_ROLES = "org.members.bucket.user.roles";
    private static final String ORG_MEMBERS_BUCKET_TYPE = "org.members.bucket.type";
    private static final String ORG_MEMBERS_BUCKET_QUOTA = "org.members.bucket.quota";
    private static final String ORG_MEMBERS_BUCKET_REPLICAS = "org.members.bucket.replicas";
    private static final String ORG_MEMBERS_BUCKET_INDEX_REPLICAS = "org.members.bucket.indexReplicas";
    private static final String ORG_MEMBERS_BUCKET_ENABLE_FLUSH = "org.members.bucket.enableFlush";
    private List<String> clusterNodes;
    private List<String> orgMemberUserRoles;
    private final Properties settings;

    private CouchbaseSettings() {
        settings = new Properties();

        try {
            settings.load(getClass().getResourceAsStream("/settings.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize", e);
        }
    }

    BucketType getOrgMembersBucketType() {
        return Enum.valueOf(BucketType.class, getProperty(ORG_MEMBERS_BUCKET_TYPE));
    }

    int getOrgMembersBucketQuota() {
        return Integer.valueOf(getProperty(ORG_MEMBERS_BUCKET_QUOTA));
    }

    int getOrgMembersBucketReplicas() {
        return Integer.valueOf(getProperty(ORG_MEMBERS_BUCKET_REPLICAS));
    }

    boolean getOrgMembersBucketIndexReplicas() {
        return Boolean.valueOf(getProperty(ORG_MEMBERS_BUCKET_INDEX_REPLICAS));
    }

    boolean getOrgMembersBucketEnableFlush() {
        return Boolean.valueOf(getProperty(ORG_MEMBERS_BUCKET_ENABLE_FLUSH));
    }

    String getCouchbaseAdmin() {
        return getProperty(COUCHBASE_ADMIN);
    }

    String getCouchbaseAdminPassword() {
        return getProperty(COUCHBASE_ADMIN_PASSWORD);
    }

    List<String> getCouchbaseClusterNodes() {
        clusterNodes = getList(COUCHBASE_CLUSTER_NODES, clusterNodes);
        return clusterNodes;
    }

    List<String> getOrgMemberUserRoles() {
        orgMemberUserRoles = getList(ORG_MEMBERS_USER_ROLES, orgMemberUserRoles);
        return orgMemberUserRoles;
    }


    private List<String> getList(String key, List<String> list) {
        if (list == null) {
            String value = getProperty(key);
            list = value.length() > 0 ? Arrays.asList(value.split(",")) : Collections.EMPTY_LIST;
        }
        return list;
    }

    String getOrgMembersBucketSuffix() {
        return getProperty(ORG_MEMBERS_BUCKET_SUFFIX);
    }


    String getProperty(String key) {
        return settings.getProperty(key);
    }

    static class Builder {
        public CouchbaseSettings build() {
            return new CouchbaseSettings();
        }
    }
}
