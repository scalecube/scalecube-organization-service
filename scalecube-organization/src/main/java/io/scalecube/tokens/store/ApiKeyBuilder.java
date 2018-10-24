package io.scalecube.tokens.store;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Role;
import io.scalecube.config.AppConfiguration;
import io.scalecube.tokens.JwtApiKey;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Represent a class that encapsulate the logic of constructing an ApiKey.
 */
public final class ApiKeyBuilder {

  private static final String ROLE_KEY = "role";
  private static final String ISSUER = "scalecube.io";
  private static final String TOKEN_EXPIRATION = "token.expiration";

  /**
   * Builds an APiKey based on the <code>organization</code>, <ocde>claims</ocde>
   * and <code>apiKeyName</code> arguments.
   * @param organization the organization of the generated API key.
   * @param claims the generated API key token claims
   * @param apiKeyName the generated API Key name
   * @return a signed ApiKey instance
   */
  public static ApiKey build(Organization organization,
      Map<String, String> claims,
      String apiKeyName) {
    Map<String, String> tokenClaims = claims == null ? new HashMap<>() : claims;

    if (!tokenClaims.containsKey(ROLE_KEY) || !isRoleValid(tokenClaims.get(ROLE_KEY))) {
      // add minimal role
      tokenClaims.put(ROLE_KEY, Role.Member.toString());
    }
    return JwtApiKey.builder().issuer(ISSUER)
        .subject(organization.id())
        .name(apiKeyName)
        .claims(tokenClaims)
        .id(organization.id())
        .audience(organization.id())
        .expiration(tryGetTokenExpiration())
        .build(organization.secretKeyId(), organization.secretKey());
  }

  private static boolean isRoleValid(String role) {
    try {
      Enum.valueOf(Role.class, role);
    } catch (Throwable ex) {
      return false;
    }
    return true;
  }

  private static long tryGetTokenExpiration() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    long amount = 2678399982L;

    try {
      amount = Long.parseLong(AppConfiguration.builder().build().getProperty(TOKEN_EXPIRATION));
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
    }

    calendar.setTimeInMillis(System.currentTimeMillis() + amount);
    return calendar.getTimeInMillis();
  }
}
