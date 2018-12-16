package io.scalecube.account.api;

/**
 * Represents an invite a member to an Organization request.
 */
public class InviteOrganizationMemberRequest {

  private Token token;
  private String organizationId;
  private String userId;
  private String role;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  InviteOrganizationMemberRequest() {
  }

  /**
   * Constructs an invite a member to an Organization request.
   *
   * @param token Verification Id.
   * @param organizationId Organization Id.
   * @param userId Invited user's Id.
   */
  public InviteOrganizationMemberRequest(Token token, String organizationId, String userId) {
    this(token, organizationId, userId, Role.Member.toString());
  }

  /**
   * Constructs an invite a member to an Organization request.
   *
   * @param token Verification Id.
   * @param organizationId Organization Id.
   * @param userId Invited user's Id.
   */
  public InviteOrganizationMemberRequest(Token token, String organizationId, String userId,
                                         String role) {
    this.token = token;
    this.organizationId = organizationId;
    this.userId = userId;
    this.role = role;
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

  public String role() {
    return this.role;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [userId=%s, organizationId=%s, token=%s, role=%s]",
        userId, organizationId, token, role);
  }
}
