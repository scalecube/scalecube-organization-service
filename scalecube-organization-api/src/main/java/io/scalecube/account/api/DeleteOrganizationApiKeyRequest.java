package io.scalecube.account.api;

public class DeleteOrganizationApiKeyRequest {

  private final Token token;
  private final String organizationId;
  private final String apiKeyName;

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
