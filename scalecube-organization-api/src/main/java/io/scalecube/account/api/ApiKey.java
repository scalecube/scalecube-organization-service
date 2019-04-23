package io.scalecube.account.api;

import java.util.Map;

public class ApiKey {

  protected String keyId;
  protected String name;
  protected Map<String, String> claims;
  protected String key;

  public String keyId() {
    return this.keyId;
  }

  public String name() {
    return this.name;
  }

  public Map<String, String> claims() {
    return this.claims;
  }

  public String key() {
    return this.key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ApiKey apiKey = (ApiKey) o;

    if (keyId != null ? !keyId.equals(apiKey.keyId) : apiKey.keyId != null) {
      return false;
    }
    if (name != null ? !name.equals(apiKey.name) : apiKey.name != null) {
      return false;
    }
    if (claims != null ? !claims.equals(apiKey.claims) : apiKey.claims != null) {
      return false;
    }
    return key != null ? key.equals(apiKey.key) : apiKey.key == null;
  }

  @Override
  public int hashCode() {
    int result = keyId != null ? keyId.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (claims != null ? claims.hashCode() : 0);
    result = 31 * result + (key != null ? key.hashCode() : 0);
    return result;
  }
}
