package io.scalecube.account.api;


/**
 * Represents a request to create a new organization.
 */
public class CreateOrganizationRequest {

  private String name;

  private Token token;

  private String email;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  CreateOrganizationRequest() {
  }

  /**
   * Constructs a request to create a new organization.
   *
   * @param name New organization name
   * @param token Verification token
   * @param email Organization owner's email
   */
  public CreateOrganizationRequest(String name, Token token, String email) {
    this.name = name;
    this.token = token;
    this.email = email;
  }

  public String name() {
    return name;
  }

  public Token token() {
    return token;
  }

  public String email() {
    return email;
  }

  @Override
  public String toString() {
    return super.toString()
        + String.format(" [name=%s, email=%s, token=%s]",
            name,email, token);
  }
}
