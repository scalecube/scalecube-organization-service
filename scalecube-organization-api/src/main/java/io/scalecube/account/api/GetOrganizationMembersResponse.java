package io.scalecube.account.api;

/** Get organization membership response. */
public class GetOrganizationMembersResponse {

  private OrganizationMember[] members;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  GetOrganizationMembersResponse() {}

  /**
   * Constructs a get organization membership API call response object.
   *
   * @param members array of organization members objects.
   */
  public GetOrganizationMembersResponse(OrganizationMember[] members) {
    this.members = members;
  }

  public OrganizationMember[] members() {
    return this.members;
  }
}
