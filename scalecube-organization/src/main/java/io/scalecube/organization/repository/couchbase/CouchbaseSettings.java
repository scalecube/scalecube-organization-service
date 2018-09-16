package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.bucket.BucketType;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.organization.repository.exception.DataAccessResourceFailureException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

final class CouchbaseSettings {

  private static final String COUCHBASE_ADMIN = "couchbase.admin.user";
  private static final String COUCHBASE_ADMIN_PASSWORD = "couchbase.admin.password";
  private static final String COUCHBASE_CLUSTER_NODES = "couchbase.cluster.nodes";
  private static final String ORG_MEMBERS_BUCKET_SUFFIX = "org.members.bucket.suffix";
  private static final String ORG_MEMBERS_USER_ROLES = "org.members.bucket.user.roles";
  private static final String ORG_MEMBERS_BUCKET_TYPE = "org.members.bucket.type";
  private static final String ORG_MEMBERS_BUCKET_QUOTA = "org.members.bucket.quota";
  private static final String ORG_MEMBERS_BUCKET_REPLICAS = "org.members.bucket.replicas";
  private static final String ORG_MEMBERS_BUCKET_INDEX_REPLICAS =
      "org.members.bucket.indexReplicas";
  private static final String ORG_MEMBERS_BUCKET_ENABLE_FLUSH = "org.members.bucket.enableFlush";
  private final Properties settings;
  private List<String> clusterNodes;
  private List<String> orgMemberUserRoles;
  private final CouchbaseProperties couchbaseProperties;


  private CouchbaseSettings() {
    settings = new Properties();

    try {
      couchbaseProperties = ConfigRegistryConfiguration.configRegistry()
          .objectProperty("couchbase", CouchbaseProperties.class).value().get();
      Objects.requireNonNull(couchbaseProperties, "failed to get couchbase properties");
      settings.load(getClass().getResourceAsStream("/couchbase-settings.properties"));
    } catch (Exception ex) {
      throw new DataAccessResourceFailureException("Failed to initialize", ex);
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

  String getCouchbaseUsername() {
    return couchbaseProperties.username();
  }

  String getCouchbasePassword() {
    return couchbaseProperties.password();
  }

  List<String> getCouchbaseClusterNodes() {
    return couchbaseProperties.hosts() == null
        ? Collections.EMPTY_LIST
        : couchbaseProperties.hosts();
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
