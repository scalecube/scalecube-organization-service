package io.scalecube.organization.domain;

public abstract class Entity {

  protected String id;
  private long version;

  public String id() {
    return id;
  }

  public long version() {
    return version;
  }

  public void version(long version) {
    this.version = version;
  }
}
