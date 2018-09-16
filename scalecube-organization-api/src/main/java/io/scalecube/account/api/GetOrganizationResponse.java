package io.scalecube.account.api;

/**
 * Represents a response to a get Organization API call.
 */
public class GetOrganizationResponse extends OrganizationInfo {

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  GetOrganizationResponse() {
  }

  /**
   * Constructs a response to a get Organization API call.
   *
   * @param id Organization Id.
   * @param name Organization name.
   * @param apiKeys Organization API keys.
   * @param email Organization email
   * @param ownerId Organization owner's Id.
   */
  public GetOrganizationResponse(
      String id,
      String name,
      ApiKey[] apiKeys,
      String email,
      String ownerId) {
    super(id, name, apiKeys, email, ownerId);
  }

}
