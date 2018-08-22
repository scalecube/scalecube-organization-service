package io.scalecube.account.api;

/**
 * Create organization API call response.
 */
public class CreateOrganizationResponse extends OrganizationInfo {

  /**
   * Default constructor.
   * @deprecated only for serialization/deserialization.
   */
  CreateOrganizationResponse() {}

  /**
   * Constructs a response tio a call to the Create organization API.
   * @param id New organization Id.
   * @param name New Organization name.
   * @param apiKey New Organization API keys.
   * @param email New Organization owner's email.
   * @param ownerId New Organization owner's Id.
   */
  public CreateOrganizationResponse(
          String id,
          String name,
          ApiKey[] apiKey,
          String email,
          String ownerId) {
    super(id, name, apiKey, email, ownerId);
  }

}
