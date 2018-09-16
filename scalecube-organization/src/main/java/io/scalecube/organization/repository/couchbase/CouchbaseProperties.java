package io.scalecube.organization.repository.couchbase;

import java.util.List;

public final class CouchbaseProperties {

  private List<String> hosts;
  private String username;
  private String password;

  public List<String> hosts() {
    return hosts;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }
}