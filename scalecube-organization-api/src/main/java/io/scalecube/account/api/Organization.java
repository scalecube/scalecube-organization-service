package io.scalecube.account.api;

/**
 * Represents an Organization.
 */
public class Organization {

  private String name;

  private ApiKey[] apiKeys;

  private String id;

  private String ownerId;

  private String secretKey;

  private String email;

  private String secretKeyId;

  /**
   * Constructs an empty Organization object.
   */
  Organization() {
  }

  private Organization(String id, String name, String ownerId, String secretKeyId, String secretKey,
      ApiKey[] apiKeys, String email) {
    this.id = id;
    this.ownerId = ownerId;
    this.secretKeyId = secretKeyId;
    this.secretKey = secretKey;
    this.apiKeys = apiKeys;
    this.name = name;
    this.email = email;
  }

  public String ownerId() {
    return this.ownerId;
  }

  public String id() {
    return this.id;
  }

  public String name() {
    return this.name;
  }

  public ApiKey[] apiKeys() {
    return this.apiKeys;
  }

  public String secretKeyId() {
    return this.secretKeyId;
  }

  public String secretKey() {
    return this.secretKey;
  }

  public String email() {
    return this.email;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String ownerId;

    private String name;

    private ApiKey[] apiKeys = {};

    private String secretKey;

    private String email;

    private String id;

    private String secretKeyId;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder ownerId(String ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder apiKey(ApiKey[] apiKeys) {
      this.apiKeys = apiKeys;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder secretKeyId(String secretKeyId) {
      this.secretKeyId = secretKeyId;
      return this;
    }


    public Builder secretKey(String secretKey) {
      this.secretKey = secretKey;
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
      return new Organization(source.id(), name, source.ownerId(), source.secretKeyId(),
          source.secretKey(), apiKeys, email);
    }

    public Organization build() {
      return new Organization("ORG-" + this.id, this.name, this.ownerId, this.secretKeyId,
          this.secretKey, this.apiKeys, this.email);
    }
  }

  @Override
  public String toString() {
    return "Organization [name=" + name + ", apiKey=" + apiKeys + ", id=" + id
        + ", ownerId=" + ownerId + "]";
  }
}
