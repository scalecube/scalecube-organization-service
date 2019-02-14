package io.scalecube.account.api;

/** Represents a request to create a new organization. */
public class CreateOrganizationRequest {

  private String name;
  private String email;
  private Token token;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  CreateOrganizationRequest() {}

  /**
   * Constructs a request to create a new organization.
   *
   * @param name New organization name
   * @param token Verification token
   */
  public CreateOrganizationRequest(String name, String email, Token token) {
    this.name = name;
    this.email = email;
    this.token = token;
  }

  public String name() {
    return name;
  }

  public String email() {
    return email;
  }

  public Token token() {
    return token;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [name=%s, email=%s, token=%s]", name, email, token);
  }
}
