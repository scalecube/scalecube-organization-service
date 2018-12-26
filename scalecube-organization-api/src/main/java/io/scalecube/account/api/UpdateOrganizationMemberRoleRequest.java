package io.scalecube.account.api;

/**
 * Represents a request to update a membership role.
 */
public class UpdateOrganizationMemberRoleRequest {
  private Token token;

  private String organizationId;

  private String userId;

  private String role;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  UpdateOrganizationMemberRoleRequest() {
  }

  /**
   * Constructs an instance of @{@link UpdateOrganizationMemberRoleRequest} with the following
   *     arguments.
   * @param token caller token
   * @param organizationId organization id criteria
   * @param userId user id criteria
   * @param role the new role
   */
  public UpdateOrganizationMemberRoleRequest(
      Token token,
      String organizationId,
      String userId,
      String role) {
    this.token = token;
    this.organizationId = organizationId;
    this.userId = userId;
    this.role = role;
  }

  public String userId() {
    return this.userId;
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
        + String.format(" [organizationId=%s, token=%s, userId=%s, role=%s]",
        organizationId, token, userId, role);
  }

  public String role() {
    return role;
  }
}
