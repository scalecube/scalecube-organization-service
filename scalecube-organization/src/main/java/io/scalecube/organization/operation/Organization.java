package io.scalecube.organization.operation;

import io.scalecube.account.api.ApiKey;

/** Represents an Organization. */
public class Organization {

  private String id;
  private String name;
  private String email;
  private String keyId;
  private ApiKey[] apiKeys;

  Organization() {}

  private Organization(String id, String name, String email, String keyId, ApiKey[] apiKeys) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.keyId = keyId;
    this.apiKeys = apiKeys;
  }

  private Organization(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.email = builder.email;
    this.keyId = builder.keyId;
    this.apiKeys = builder.apiKeys;
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String email() {
    return email;
  }

  public String keyId() {
    return keyId;
  }

  public ApiKey[] apiKeys() {
    return apiKeys;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String id;
    private String name;
    private String email;
    private String keyId;
    private ApiKey[] apiKeys = {};

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder keyId(String keyId) {
      this.keyId = keyId;
      return this;
    }

    public Builder apiKey(ApiKey[] apiKeys) {
      this.apiKeys = apiKeys;
      return this;
    }

    /**
     * Creates a copy of the Organization source argument.
     *
     * @param source The source to copy from
     * @return an Organization object which a shallow copy of the source argument.
     */
    public Organization copy(Organization source) {
      String email = this.email == null ? source.email : this.email;
      String name = this.name == null ? source.name : this.name;
      ApiKey[] apiKeys = this.apiKeys == null ? source.apiKeys : this.apiKeys;
      return new Organization(source.id(), name, email, source.keyId(), apiKeys);
    }

    public Organization build() {
      return new Organization(this);
    }
  }

  @Override
  public String toString() {
    return "Organization [id="
        + id
        + ", name="
        + name
        + ", email="
        + email
        + ", keyId="
        + keyId
        + "]";
  }
}
