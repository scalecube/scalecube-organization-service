package io.scalecube.account.api;

public final class GetPublicKeyResponse {

  private String algorithm;
  private String format;
  private byte[] key;
  private String keyId;

  /**
   * Only for serialization/deserialization.
   *
   * @deprecated for instantiation purposes.
   */
  GetPublicKeyResponse() {}

  /**
   * Creates new instance with information about public key.
   *
   * @param algorithm the algorithm used to generate key pair
   * @param format the format of public key
   * @param key the encoded key
   * @param keyId the key identifier
   */
  public GetPublicKeyResponse(String algorithm, String format, byte[] key, String keyId) {
    this.algorithm = algorithm;
    this.format = format;
    this.key = key;
    this.keyId = keyId;
  }

  public String algorithm() {
    return algorithm;
  }

  public String format() {
    return format;
  }

  public byte[] key() {
    return key;
  }

  public String keyId() {
    return keyId;
  }
}
