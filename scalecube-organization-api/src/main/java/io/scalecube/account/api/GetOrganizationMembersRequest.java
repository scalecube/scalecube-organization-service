package io.scalecube.account.api;

public class GetOrganizationMembersRequest {

  private String organizationId;

  private Token token;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  GetOrganizationMembersRequest() {}

  public GetOrganizationMembersRequest(String organizationId, Token token) {
    this.organizationId = organizationId;
    this.token = token;
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
