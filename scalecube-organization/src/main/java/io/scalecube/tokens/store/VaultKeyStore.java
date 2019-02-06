package io.scalecube.tokens.store;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import io.scalecube.config.AppConfiguration;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.tokens.KeyStoreException;
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
class VaultKeyStore implements KeyStore {

  private static final String PUBLIC_KEY = "public-key";
  private static final String PRIVATE_KEY = "private-key";

  private final VaultPathBuilder vaultPathBuilder = new VaultPathBuilder();
  private Vault vault;
  private final int maxRetries;
  private static final int RETRY_INTERVAL_MILLISECONDS = 1000;
  private final int retryIntervalMilliseconds;
  private static final String VAULT_MAX_RETRIES_KEY = "vault.max.retries";
  private static final int MAX_RETRIES = 5;
  private static final String VAULT_RETRY_INTERVAL_MILLISECONDS =
      "vault.retry.interval.milliseconds";
  private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyStore.class);

  VaultKeyStore() {
    try {
      vault = new Vault(new VaultConfig().build());
    } catch (VaultException ex) {
      throw new RuntimeException(ex);
    }

    ConfigRegistry configRegistry = AppConfiguration.configRegistry();
    maxRetries = configRegistry.intValue(VAULT_MAX_RETRIES_KEY, MAX_RETRIES);
    retryIntervalMilliseconds =
        configRegistry.intValue(VAULT_RETRY_INTERVAL_MILLISECONDS, RETRY_INTERVAL_MILLISECONDS);
  }

  @Override
  public void store(String alias, KeyPair keyPair) throws KeyStoreException {
    String path = null;
    try {
      path = vaultPathBuilder.getPath(alias);
      LOGGER.debug("Writing key to Vault path: '{}'", path);
      final Map<String, Object> keys = new HashMap<>();
      keys.put(PUBLIC_KEY, encodeKey(keyPair.getPublic()));
      keys.put(PRIVATE_KEY, encodeKey(keyPair.getPrivate()));
      LogicalResponse write =
          vault.withRetries(maxRetries, retryIntervalMilliseconds).logical().write(path, keys);
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
    String path = vaultPathBuilder.getPath(keyId);

    try {
      String publicKeyEncoded =
          vault
              .withRetries(maxRetries, retryIntervalMilliseconds)
              .logical()
              .read(path)
              .getData()
              .get(PUBLIC_KEY);

      X509EncodedKeySpec publicKeySpec =
          new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyEncoded));

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
    String path = vaultPathBuilder.getPath(keyId);

    try {
      String privateKeyEncoded =
          vault
              .withRetries(maxRetries, retryIntervalMilliseconds)
              .logical()
              .read(path)
              .getData()
              .get(PRIVATE_KEY);

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

  private String encodeKey(Key key) {
    return new String(Base64.getEncoder().encode(key.getEncoded()));
  }
}
