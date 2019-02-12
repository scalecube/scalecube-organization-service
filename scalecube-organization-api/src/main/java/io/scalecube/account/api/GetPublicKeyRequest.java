package io.scalecube.account.api;

public final class GetPublicKeyRequest {

  private String keyId;

  /**
   * Only for serialization/deserialization.
   *
   * @deprecated for instantiation purposes.
   */
  GetPublicKeyRequest() {}

  public GetPublicKeyRequest(String keyId) {
    this.keyId = keyId;
  }

  public String keyId() {
    return keyId;
  }
}
