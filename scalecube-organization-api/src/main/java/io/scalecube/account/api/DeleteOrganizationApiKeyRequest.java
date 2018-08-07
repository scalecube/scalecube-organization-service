package io.scalecube.account.api;

/**
 * Delete organization API key API call request.
 */
public class DeleteOrganizationApiKeyRequest {

  private final Token token;
  private final String organizationId;
  private final String apiKeyName;

  /**
   * Construct a request object to delete an organization API key request.
   *
   * @param token Verification token.
   * @param organizationId Organization Id.
   * @param apiKeyName API key name.
   */
  public DeleteOrganizationApiKeyRequest(Token token, String organizationId, String apiKeyName) {
    this.token = token;
    this.organizationId = organizationId;
    this.apiKeyName = apiKeyName;
  }

  public Token token() {
    return this.token;
  }

  public String organizationId() {
    return this.organizationId;
  }

  public String apiKeyName() {
    return this.apiKeyName;
  }

}
