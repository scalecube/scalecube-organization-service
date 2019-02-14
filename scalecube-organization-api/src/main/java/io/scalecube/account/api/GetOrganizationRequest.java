package io.scalecube.account.api;

public class GetOrganizationRequest {

  private Token token;

  private String organizationId;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  GetOrganizationRequest() {}

  public GetOrganizationRequest(Token token, String organizationId) {
    this.token = token;
    this.organizationId = organizationId;
  }

  public Token token() {
    return this.token;
  }

  public String organizationId() {
    return this.organizationId;
  }

  @Override
  public String toString() {
    return super.toString()
        + String.format(" [organizationId=%s, token=%s]", organizationId, token);
  }
}
