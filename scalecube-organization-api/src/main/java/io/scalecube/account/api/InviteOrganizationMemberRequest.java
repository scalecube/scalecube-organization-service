package io.scalecube.account.api;

/**
 * Represents an invite a member to an Organization request.
 */
public class InviteOrganizationMemberRequest {

  private final Token token;
  private final String organizationId;
  private final String userId;

  /**
   * Constructs an invite a member to an Organization request.
   *
   * @param token Verification Id.
   * @param organizationId Organization Id.
   * @param userId Invited user's Id.
   */
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
