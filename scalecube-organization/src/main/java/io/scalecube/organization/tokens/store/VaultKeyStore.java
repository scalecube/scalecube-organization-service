package io.scalecube.organization.tokens.store;

import static io.scalecube.organization.config.AppConfiguration.VAULT_ENGINE_VERSION;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import io.scalecube.account.api.OrganizationServiceException;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.IntConfigProperty;
import io.scalecube.config.StringConfigProperty;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.tokens.KeyStoreException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HshiCorp Vault based KeyStore implementation. Vault access is done via
 * BetterCloud/vault-java-driver at https://github.com/BetterCloud/vault-java-driver. Vault address
 * and token defaults are "VAULT_ADDR" and "VAULT_TOKEN" environment variables respectively.
 * Optional values can be provided in the application settings file.
 */
public class VaultKeyStore implements KeyStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyStore.class);

  private static final String PUBLIC_KEY = "public-key";
  private static final String PRIVATE_KEY = "private-key";
  private static final int DEFAULT_MAX_RETRIES = 5;
  private static final int DEFAULT_RETRY_INTERVAL_MILLISECONDS = 1000;

  private static final ConfigRegistry configRegistry = AppConfiguration.configRegistry();

  private final IntConfigProperty maxRetries = configRegistry.intProperty("vault.max.retries");
  private final IntConfigProperty retryInterval =
      configRegistry.intProperty("vault.retry.interval.milliseconds");
  private StringConfigProperty vaultSecretsPath =
      configRegistry.stringProperty("VAULT_SECRETS_PATH");
  private StringConfigProperty apiKeysPathPattern =
      configRegistry.stringProperty("api.keys.path.pattern");

  private final Vault vault;

  /** Constructor. */
  public VaultKeyStore() {
    try {
      vault = new Vault(new VaultConfig().engineVersion(VAULT_ENGINE_VERSION).build());
    } catch (VaultException ex) {
      throw new OrganizationServiceException("Error during vault initialization", ex);
    }
  }

  @Override
  public void store(String alias, KeyPair keyPair) throws KeyStoreException {
    String path = null;
    try {
      path = getPath(alias);

      LOGGER.debug("Writing key to Vault path: '{}'", path);

      Map<String, Object> keys = new HashMap<>();

      keys.put(PUBLIC_KEY, encodeKey(keyPair.getPublic()));
      keys.put(PRIVATE_KEY, encodeKey(keyPair.getPrivate()));

      LogicalResponse write = vaultLogical().write(path, keys);

      LOGGER.debug(
          "Key written to Vault path: '{}' REST response code: '{}' ",
          path,
          write.getRestResponse().getStatus());
    } catch (VaultException ex) {
      LOGGER.error("Error writing key to Vault path: '{}' error: '{}' ", path, ex);
      throw new KeyStoreException(ex);
    }
  }

  @Override
  public PublicKey getPublicKey(String keyId) {
    String path = getPath(keyId);

    try {
      String publicKeyEncoded = vaultLogical().read(path).getData().get(PUBLIC_KEY);

      KeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyEncoded));

      return KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
    } catch (VaultException e) {
      LOGGER.error("Error reading public key from Vault path={}", path, e);
      throw new KeyStoreException(e);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      LOGGER.error("Error reconstructing public key", path, e);
      throw new KeyStoreException(e);
    }
  }

  @Override
  public PrivateKey getPrivateKey(String keyId) {
    String path = getPath(keyId);

    try {
      String privateKeyEncoded = vaultLogical().read(path).getData().get(PRIVATE_KEY);

      KeySpec privateKeySpec =
          new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyEncoded));

      return KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);
    } catch (VaultException e) {
      LOGGER.error("Error reading private key from Vault path={}", path, e);
      throw new KeyStoreException(e);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      LOGGER.error("Error reconstructing private key", path, e);
      throw new KeyStoreException(e);
    }
  }

  @Override
  public void delete(String keyId) {
    String path = getPath(keyId);

    try {
      vaultLogical().delete(path);
    } catch (VaultException e) {
      LOGGER.error("Error deleting key pair from Vault path={}", path, e);
      throw new KeyStoreException(e);
    }
  }

  private Logical vaultLogical() {
    return vault
        .withRetries(
            maxRetries.value().orElse(DEFAULT_MAX_RETRIES),
            retryInterval.value().orElse(DEFAULT_RETRY_INTERVAL_MILLISECONDS))
        .logical();
  }

  private String getPath(String alias) {
    return String.format(apiKeysPathPattern.valueOrThrow(), vaultSecretsPath.valueOrThrow())
        .concat(alias);
  }

  private String encodeKey(Key key) {
    return new String(Base64.getEncoder().encode(key.getEncoded()));
  }
}
