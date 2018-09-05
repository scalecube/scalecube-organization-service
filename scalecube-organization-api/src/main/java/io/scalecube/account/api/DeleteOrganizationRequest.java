package io.scalecube.account.api;

/**
 * Delete organization API call request.
 */
public class DeleteOrganizationRequest {

  private String organizationId;

  private Token token;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  DeleteOrganizationRequest() {
  }

  /**
   * Construct a request object to a delete an organization API call.
   *
   * @param token Verification token.
   * @param organizationId Organization Id.
   */
  public DeleteOrganizationRequest(Token token, String organizationId) {
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
