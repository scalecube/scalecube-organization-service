package io.scalecube.account.api;

public class GetOrganizationMembersRequest {

  private final String organizationId;

  private final Token token;

  public GetOrganizationMembersRequest(String organizationId,
                                       Token token) {
    this.organizationId = organizationId;
    this.token = token;
  }

  public Token token() {
    return this.token;
  }

  public String organizationId() {
    return this.organizationId;
  }

}
