package io.scalecube.account.api;

/**
 * Create organization API call response.
 */
public class CreateOrganizationResponse extends OrganizationInfo {

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  CreateOrganizationResponse() {
  }

  public CreateOrganizationResponse(
      Builder builder) {
    super(builder);
  }
}
