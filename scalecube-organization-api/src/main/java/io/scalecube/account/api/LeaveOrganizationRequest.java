package io.scalecube.account.api;

public class LeaveOrganizationRequest {

  private final Token token;

  private final String organizationId;

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
