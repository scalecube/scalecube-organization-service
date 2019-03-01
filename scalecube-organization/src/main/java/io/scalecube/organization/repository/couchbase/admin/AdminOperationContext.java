package io.scalecube.organization.repository.couchbase.admin;

import com.couchbase.client.java.Cluster;
import io.scalecube.organization.operation.Organization;
import io.scalecube.organization.repository.couchbase.CouchbaseSettings;
import java.util.Objects;

/** Represents a data structure used to execute an admin operation. */
public final class AdminOperationContext {
  private final CouchbaseSettings settings;
  private final Cluster cluster;
  private final String name;
  private final Organization organization;

  private AdminOperationContext(Builder builder) {
    this.settings = builder.settings;
    this.cluster = builder.cluster;
    this.name = builder.name;
    this.organization = builder.organization;
  }

  public static Builder builder() {
    return new Builder();
  }

  public CouchbaseSettings settings() {
    return settings;
  }

  public Cluster cluster() {
    return cluster;
  }

  public String name() {
    return name;
  }

  public Organization organization() {
    return organization;
  }

  public static class Builder {
    private CouchbaseSettings settings;
    private Cluster cluster;
    private String name;
    private Organization organization;

    public Builder settings(CouchbaseSettings settings) {
      this.settings = settings;
      return this;
    }

    public Builder cluster(Cluster cluster) {
      this.cluster = cluster;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Constructs an instance of {@link AdminOperationContext} using this builder fields.
     *
     * @return an instance of {@link AdminOperationContext}
     */
    public AdminOperationContext build() {
      Objects.requireNonNull(settings, "settings");
      Objects.requireNonNull(cluster, "cluster");
      Objects.requireNonNull(name, "name");
      return new AdminOperationContext(this);
    }

    public Builder organization(Organization organization) {
      this.organization = organization;
      return this;
    }
  }
}
