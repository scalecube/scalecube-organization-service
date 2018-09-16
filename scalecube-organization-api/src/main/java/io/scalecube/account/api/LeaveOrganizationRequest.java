package io.scalecube.account.api;

public class LeaveOrganizationRequest {

  private Token token;

  private String organizationId;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  LeaveOrganizationRequest() {
  }

  public LeaveOrganizationRequest(Token token, String organizationId) {
    this.token = token;
    this.organizationId = organizationId;
  }

  public Token token() {
    return this.token;
  }

  public String organizationId() {
    return this.organizationId;
  }

}
