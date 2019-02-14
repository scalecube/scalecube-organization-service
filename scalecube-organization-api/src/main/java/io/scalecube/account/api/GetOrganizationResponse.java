package io.scalecube.account.api;

/** Represents a response to a get Organization API call. */
public class GetOrganizationResponse extends OrganizationInfo {

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  GetOrganizationResponse() {}

  public GetOrganizationResponse(Builder builder) {
    super(builder);
  }
}
