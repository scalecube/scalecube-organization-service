package io.scalecube.account.api;

/**
 * Delete organization API call response.
 */
public class DeleteOrganizationResponse {

  private boolean deleted;
  private String organizationId;

  /**
   * Default constructor.
   * @deprecated only for serialization/deserialization.
   */
  DeleteOrganizationResponse() {}

  /**
   * Construct a response object to a delete an organization API call.
   *
   * @param organizationId Organization Id.
   * @param deleted Organization deleted indication.
   */
  public DeleteOrganizationResponse(String organizationId, boolean deleted) {
    this.deleted = deleted;
    this.organizationId = organizationId;
  }

  public boolean deleted() {
    return this.deleted;
  }

  public String organizationId() {
    return this.organizationId;
  }
}
