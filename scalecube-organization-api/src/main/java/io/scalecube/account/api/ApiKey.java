package io.scalecube.account.api;

import java.util.Map;
import java.util.Objects;

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
    return Objects.equals(keyId, apiKey.keyId)
        && Objects.equals(name, apiKey.name)
        && Objects.equals(claims, apiKey.claims)
        && Objects.equals(key, apiKey.key);
  }

  @Override
  public int hashCode() {
    int result = keyId != null ? keyId.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (claims != null ? claims.hashCode() : 0);
    result = 31 * result + (key != null ? key.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ApiKey{"
        + "keyId='"
        + keyId
        + "\', name='"
        + name
        + "\', claims="
        + claims
        + ", key='"
        + key
        + "\'}";
  }
}
