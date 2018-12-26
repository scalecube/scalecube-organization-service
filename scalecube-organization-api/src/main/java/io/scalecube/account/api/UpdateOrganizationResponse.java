package io.scalecube.account.api;

/**
 * Represents a response of an update of an organization.
 */
public class UpdateOrganizationResponse extends OrganizationInfo {

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  UpdateOrganizationResponse() {
  }

  public UpdateOrganizationResponse(Builder builder) {
    super(builder);
  }
}
