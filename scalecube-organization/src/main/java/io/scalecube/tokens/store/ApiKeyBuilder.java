package io.scalecube.tokens.store;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.Role;
import io.scalecube.config.AppConfiguration;
import io.scalecube.config.LongConfigProperty;
import io.scalecube.organization.Organization;
import io.scalecube.tokens.JwtApiKey;
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
   * @param organization organization context of the API key.
   * @param request context for creating the API key.
   * @return a signed ApiKey instance
   */
  public static ApiKey build(Organization organization, AddOrganizationApiKeyRequest request) {
    final Map<String, String> tokenClaims =
        request.claims() == null || request.claims().isEmpty() ? new HashMap<>() : request.claims();

    if (!tokenClaims.containsKey(ROLE_KEY) || !isRoleValid(tokenClaims.get(ROLE_KEY))) {
      // add minimal role
      tokenClaims.put(ROLE_KEY, Role.Member.toString());
    }

    PrivateKey signingKey = KeyStoreFactory.get().getPrivateKey(organization.keyId());

    return JwtApiKey.builder()
        .issuer(ISSUER)
        .subject(organization.id())
        .name(request.apiKeyName())
        .claims(tokenClaims)
        .id(organization.id())
        .audience(organization.id())
        .expiration(tokenExpiration.value().orElse(null))
        .build(organization.keyId(), signingKey);
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
