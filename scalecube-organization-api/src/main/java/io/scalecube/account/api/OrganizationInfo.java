package io.scalecube.account.api;

/** Organization information data transfer object. */
public class OrganizationInfo {

  private ApiKey[] apiKeys;
  private String id;
  private String name;
  private String email;

  public OrganizationInfo() {}

  protected OrganizationInfo(Builder builder) {
    this.apiKeys = builder.apiKeys;
    this.id = builder.id;
    this.email = builder.email;
    this.name = builder.name;
  }

  @Override
  public String toString() {
    return super.toString()
        + String.format(
            " [id=%s, name=%s, apiKeys=%s, email=%s]", id(), name(), apiKeys().length, email());
  }

  public ApiKey[] apiKeys() {
    return this.apiKeys;
  }

  public String id() {
    return this.id;
  }

  public String email() {
    return this.email;
  }

  public String name() {
    return this.name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private ApiKey[] apiKeys;
    private String id;
    private String name;
    private String email;

    public Builder apiKeys(ApiKey[] apiKeys) {
      this.apiKeys = apiKeys;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public OrganizationInfo build() {
      return new OrganizationInfo(this);
    }
  }
}
