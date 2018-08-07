package io.scalecube.account.api;

/**
 * Represents a request to update an organization.
 */
public class UpdateOrganizationRequest {

  private final Token token;

  private final String name;

  private final String email;

  private final String organizationId;

  /**
   * Constructs a request to update an organization with the following arguments.
   *
   * @param organizationId Organization Id.
   * @param token Verification token.
   * @param name Organization name.
   * @param email Organization email.
   */
  public UpdateOrganizationRequest(String organizationId, Token token,
      String name, String email) {
    this.organizationId = organizationId;
    this.token = token;
    this.name = name;
    this.email = email;
  }


  public String name() {
    return this.name;
  }

  public String email() {
    return this.email;
  }

  public Token token() {
    return this.token;
  }

  public String organizationId() {
    return this.organizationId;
  }
}
