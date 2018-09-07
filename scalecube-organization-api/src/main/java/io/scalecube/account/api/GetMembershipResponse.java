package io.scalecube.account.api;

import java.util.Arrays;

/**
 * Get membership response.
 */
public class GetMembershipResponse {

  private OrganizationInfo[] organizations;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  GetMembershipResponse() {
  }

  /**
   * Constructs a get membership API call response object.
   *
   * @param organizationInfos array of organization info objects.
   */
  public GetMembershipResponse(OrganizationInfo[] organizationInfos) {
    this.organizations = organizationInfos;
  }

  public OrganizationInfo[] organizations() {
    return organizations;
  }

  @Override
  public String toString() {
    return "GetOrganizationsResponse [organizations=" + Arrays.toString(organizations) + "]";
  }
}
