package io.scalecube.account.api;

import java.util.Map;


/**
 * Represents a request to add organization API key.
 */
public class AddOrganizationApiKeyRequest {

  private final Token token;
  private final String organizationId;
  private final String apiKeyName;
  private final Map<String, String> claims;

  /**
   * Constructs a request to add organization API key.
   * @param token Verification token
   * @param organizationId Organization Id
   * @param apiKeyName API Key Name
   * @param claims API Key claims
   */
  public AddOrganizationApiKeyRequest(Token token,
                                      String organizationId,
                                      String apiKeyName,
                                      Map<String, String> claims) {
    this.token = token;
    this.organizationId = organizationId;
    this.apiKeyName = apiKeyName;
    this.claims = claims;
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

  public Map<String, String> claims() {
    return this.claims;
  }

}
