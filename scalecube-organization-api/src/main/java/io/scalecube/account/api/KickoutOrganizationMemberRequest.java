package io.scalecube.account.api;

public class KickoutOrganizationMemberRequest {

  private final Token token;
  private final String organizationId;
  private final String userId;

  public KickoutOrganizationMemberRequest(String organisationId, Token token, String userID) {
    this.organizationId = organisationId;
    this.token = token;
    this.userId= userID;
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
