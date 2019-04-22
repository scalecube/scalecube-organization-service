package io.scalecube.organization.tokens.store;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.Role;
import io.scalecube.config.LongConfigProperty;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.domain.Organization;
import io.scalecube.organization.tokens.JwtApiKey;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/** Represent a class that encapsulate the logic of constructing an ApiKey. */
public final class ApiKeyBuilder {

  private static final String ROLE_KEY = "role";
  private static final String ISSUER = "scalecube.io";
  private static final LongConfigProperty tokenExpiration =
      AppConfiguration.configRegistry().longProperty("token.expiration");

  /**
   * Builds an APiKey based on the <code>organization</code>, <ocde>claims</ocde> and <code>
   * apiKeyName</code> arguments.
   *
   * @param signingKey private key of key pair.
   * @param orgId organization Id.
   * @param keyId generated keyId for storage.
   * @param request context for creating the API key.
   * @return a signed ApiKey instance
   */
  public static ApiKey build(
      PrivateKey signingKey, String orgId, String keyId, AddOrganizationApiKeyRequest request) {
    final Map<String, String> tokenClaims =
        request.claims() == null || request.claims().isEmpty() ? new HashMap<>() : request.claims();

    if (!tokenClaims.containsKey(ROLE_KEY) || !isRoleValid(tokenClaims.get(ROLE_KEY))) {
      // add minimal role
      tokenClaims.put(ROLE_KEY, Role.Member.toString());
    }

    return JwtApiKey.builder()
        .issuer(ISSUER)
        .subject(orgId)
        .name(request.apiKeyName())
        .claims(tokenClaims)
        .id(orgId)
        .audience(orgId)
        .expiration(tokenExpiration.value().orElse(null))
        .keyId(keyId)
        .signingKey(signingKey)
        .build();
  }

  private static boolean isRoleValid(String role) {
    try {
      Enum.valueOf(Role.class, role);
    } catch (Throwable ex) {
      return false;
    }
    return true;
  }
}
