package io.scalecube.account.api;

/**
 * Represents a request to expel a member from an Organization.
 */
public class KickoutOrganizationMemberRequest {

  private final Token token;
  private final String organizationId;
  private final String userId;

  /**
   * Constructs a request to expel a member from an Organization.
   *
   * @param organisationId Organization Id.
   * @param token Verification token.
   * @param userId The expelled user Id.
   */
  public KickoutOrganizationMemberRequest(String organisationId, Token token, String userId) {
    this.organizationId = organisationId;
    this.token = token;
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
