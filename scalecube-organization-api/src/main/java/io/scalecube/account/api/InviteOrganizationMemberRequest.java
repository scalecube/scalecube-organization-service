package io.scalecube.account.api;

public class InviteOrganizationMemberRequest {

  private final Token token;
  private final String organizationId;
  private final String userId;

  public InviteOrganizationMemberRequest(Token token, String organizationId, String userId) {
    this.token = token;
    this.organizationId = organizationId;
    this.userId = userId;
  }

  public Token token() {
    return this.token;
  }

  public String organizationId() {
    return this.organizationId;
  }

  public String userId() {
    return this.userId;
  }

}
