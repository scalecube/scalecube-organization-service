package io.scalecube.account.api;

/**
 * Represents a response of an update of an organization.
 */
public class UpdateOrganizationResponse extends OrganizationInfo {

  /**
   * @deprecated only for serialization/deserialization.
   */
  UpdateOrganizationResponse() {}

  /**
   * Constructs a response to an update of an organization.
   * @param id Organization Id.
   * @param name Organization name.
   * @param apiKey Organization API key.
   * @param email Organization email.
   * @param ownerId Organization owner Id.
   */
  public UpdateOrganizationResponse(String id, String name, ApiKey[] apiKey,
      String email, String ownerId) {
    super(id, name, apiKey, email, ownerId);
  }
}
