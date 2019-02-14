package io.scalecube.account.api;

/** Represents a request to create a new organization. */
public class CreateOrganizationRequest {

  private String name;

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
  public CreateOrganizationRequest(String name, Token token) {
    this.name = name;
    this.token = token;
  }

  public String name() {
    return name;
  }

  public Token token() {
    return token;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [name=%s, token=%s]", name, token);
  }
}
