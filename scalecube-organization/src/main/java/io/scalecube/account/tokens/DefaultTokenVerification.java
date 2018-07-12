package io.scalecube.account.tokens;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.Token;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.jwt.WebToken;

import io.jsonwebtoken.Claims;
import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultTokenVerification implements TokenVerifier {

  private ConcurrentMap<String, WebToken> jwtProviders = new ConcurrentHashMap<>();

  private final OrganizationsDataAccess repository;

  public DefaultTokenVerification(OrganizationsDataAccess organizations) {
    this.repository = organizations;
  }

  @Override
  public User verify(Token token) {
    String key = "account-service/" + token.origin();
    Organization org = null;
    try {
      org = repository.getOrganization(token.origin());
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
    }

    if (org != null) {
      String id  = org.id();
      WebToken jwt = jwtProviders.computeIfAbsent(key, j -> new WebToken("account-service", id));
      Claims claims = jwt.parse(token.token(), org.secretKey());
      Map<String, String> claimsMap = new HashMap<String, String>();

      for (Entry<String, Object> entry : claims.entrySet()) {
        claimsMap.put(entry.getKey(), entry.getValue().toString());
      }

      if (claims != null) {
        return new User(org.id(), org.email(), true, org.name(), null, null, null, null, claimsMap);
      }
    }
    return null;
  }
}
