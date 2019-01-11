package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.bucket.BucketType;
import java.util.List;

public final class CouchbaseSettings {

  private List<String> hosts;
  private String username;
  private String password;
  private List<String> userRoles;
  private String bucketNamePattern;
  private String bucketType;
  private int bucketQuota;
  private int bucketReplicas;
  private boolean bucketIndexReplicas;
  private boolean bucketEnableFlush;
  private String organizationsBucketName;

  public List<String> hosts() {
    return hosts;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public List<String> userRoles() {
    return userRoles;
  }

  public String bucketNamePattern() {
    return bucketNamePattern;
  }

  public BucketType bucketType() {
    return BucketType.valueOf(bucketType);
  }

  public int bucketQuota() {
    return bucketQuota;
  }

  public int bucketReplicas() {
    return bucketReplicas;
  }

  public boolean bucketIndexReplicas() {
    return bucketIndexReplicas;
  }

  public boolean bucketEnableFlush() {
    return bucketEnableFlush;
  }

  public String organizationsBucketName() {
    return organizationsBucketName;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CouchbaseSettings{");
    sb.append("hosts=").append(hosts);
    sb.append(", username='").append(username).append('\'');
    sb.append(", password='").append(password).append('\'');
    sb.append(", userRoles=").append(userRoles);
    sb.append(", bucketNamePattern='").append(bucketNamePattern).append('\'');
    sb.append(", bucketType='").append(bucketType).append('\'');
    sb.append(", bucketQuota=").append(bucketQuota);
    sb.append(", bucketReplicas=").append(bucketReplicas);
    sb.append(", bucketIndexReplicas=").append(bucketIndexReplicas);
    sb.append(", bucketEnableFlush=").append(bucketEnableFlush);
    sb.append(", organizationsBucketName='").append(organizationsBucketName).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
