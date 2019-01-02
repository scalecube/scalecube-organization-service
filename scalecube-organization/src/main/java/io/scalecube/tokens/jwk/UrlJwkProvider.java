package io.scalecube.tokens.jwk;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Jwk provider that loads them from a {@link URL}.
 */
public class UrlJwkProvider implements JwkProvider {

  private static final String WELL_KNOWN_JWKS_PATH = "/.well-known/jwks.json";

  private final URL url;
  private final Integer connectTimeout;
  private final Integer readTimeout;

  /**
   * Creates a provider that loads from the given URL.
   *
   * @param url to load the jwks
   */
  private UrlJwkProvider(URL url) {
    this(url, null, null);
  }

  /**
   * Creates a provider that loads from the given URL.
   *
   * @param url to load the jwks
   * @param connectTimeout connection timeout in milliseconds (null for default)
   * @param readTimeout read timeout in milliseconds (null for default)
   */
  private UrlJwkProvider(URL url, Integer connectTimeout, Integer readTimeout) {
    Objects.requireNonNull(url, "A non-null url is required");

    if (connectTimeout != null && connectTimeout <= 0) {
      throw new IllegalArgumentException("Invalid connect timeout value '" + connectTimeout
          + "'. Must be a non-negative integer.");
    }

    if (readTimeout != null && readTimeout <= 0) {
      throw new IllegalArgumentException("Invalid read timeout value '" + readTimeout
          + "'. Must be a non-negative integer.");
    }

    this.url = url;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  /**
   * Creates a provider that loads from the given domain's well-known directory.
   * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain
   * 'samples.auth0.com'.
   * If the protocol (http or https) is not provided then https is used by default. The default jwks
   * path "/.well-known/jwks.json" is appended to the given string domain.
   * <br><br> For example, when the domain is "samples.auth0.com"
   * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
   * <br><br> Use {@link #UrlJwkProvider(URL)} if you need to pass a full URL.
   *
   * @param domain where jwks is published
   */
  public UrlJwkProvider(String domain) {
    this(urlForDomain(domain));
  }

  private static URL urlForDomain(String inputDomain) {
    Objects.requireNonNull(inputDomain, "A domain is required");
    String domain = inputDomain;

    if (!inputDomain.startsWith("http")) {
      domain = "https://" + inputDomain;
    }

    try {
      final URL url = new URL(domain);
      return new URL(url, WELL_KNOWN_JWKS_PATH);
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException("Invalid jwks uri", ex);
    }
  }

  private Map<String, Object> getJwks() throws SigningKeyNotFoundException {
    try {
      final URLConnection c = this.url.openConnection();
      if (connectTimeout != null) {
        c.setConnectTimeout(connectTimeout);
      }
      if (readTimeout != null) {
        c.setReadTimeout(readTimeout);
      }
      final InputStream inputStream = c.getInputStream();
      final JsonFactory factory = new JsonFactory();
      final JsonParser parser = factory.createParser(inputStream);
      final TypeReference<Map<String, Object>> typeReference =
          new TypeReference<Map<String, Object>>() {
          };
      return new ObjectMapper().reader().readValue(parser, typeReference);
    } catch (IOException ex) {
      throw new SigningKeyNotFoundException("Cannot obtain jwks from url " + url.toString(), ex);
    }
  }

  private List<Jwk> getAll() throws SigningKeyNotFoundException {
    List<Jwk> jwks = new ArrayList<>();
    @SuppressWarnings("unchecked") final List<Map<String, Object>> keys
        = (List<Map<String, Object>>) getJwks().get("keys");

    if (keys == null || keys.isEmpty()) {
      throw new SigningKeyNotFoundException("No keys found in " + url.toString(), null);
    }

    try {
      for (Map<String, Object> values : keys) {
        jwks.add(Jwk.fromValues(values));
      }
    } catch (IllegalArgumentException ex) {
      throw new SigningKeyNotFoundException("Failed to parse jwk from json", ex);
    }
    return jwks;
  }

  @Override
  public Jwk get(String keyId) throws SigningKeyNotFoundException {
    final List<Jwk> jwks = getAll();
    if (keyId == null && jwks.size() == 1) {
      return jwks.get(0);
    }
    if (keyId != null) {
      for (Jwk jwk : jwks) {
        if (keyId.equals(jwk.getId())) {
          return jwk;
        }
      }
    }
    throw new SigningKeyNotFoundException("No key found in "
        + url.toString() + " with kid " + keyId, null);
  }
}
