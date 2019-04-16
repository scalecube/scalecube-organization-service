package io.scalecube.organization.repository.couchbase;

import java.util.List;
import java.util.StringJoiner;

public final class CouchbaseSettings {

  private List<String> hosts;
  private String username;
  private String password;
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

  public String organizationsBucketName() {
    return organizationsBucketName;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CouchbaseSettings.class.getSimpleName() + "[", "]")
        .add("hosts=" + hosts)
        .add("username='" + username + "'")
        .add("password='" + password + "'")
        .add("organizationsBucketName='" + organizationsBucketName + "'")
        .toString();
  }
}
