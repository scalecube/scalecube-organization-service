package io.scalecube.tokens.store;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.tokens.KeyStoreException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HshiCorp Vault based KeyStore implementation. Vault access is done via
 * BetterCloud/vault-java-driver at https://github.com/BetterCloud/vault-java-driver.
 * Vault address and token defaults are "VAULT_ADDR" and "VAULT_TOKEN" environment variables
 * respectively. Optional values can be provided in the application settings file.
 */
class VaultKeyStore implements KeyStore {

  private static final String VAULT_ENTRY_KEY = "key";
  private final VaultPathBuilder vaultPathBuilder = new VaultPathBuilder();
  private Vault vault;
  private final int maxRetries;
  private static final int RETRY_INTERVAL_MILLISECONDS = 1000;
  private final int retryIntervalMilliseconds;
  private static final String VAULT_MAX_RETRIES_KEY = "vault.max.retries";
  private static final int MAX_RETRIES = 5;
  private static final String VAULT_RETRY_INTERVAL_MILLISECONDS
      = "vault.retry.interval.milliseconds";
  private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyStore.class);

  VaultKeyStore() {
    try {
      vault = new Vault(new VaultConfig().build());
    } catch (VaultException ex) {
      throw new RuntimeException(ex);
    }

    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    maxRetries = configRegistry.intValue(VAULT_MAX_RETRIES_KEY, MAX_RETRIES);
    retryIntervalMilliseconds = configRegistry.intValue(VAULT_RETRY_INTERVAL_MILLISECONDS,
        RETRY_INTERVAL_MILLISECONDS);
  }

  @Override
  public void store(String alias, Object key) throws KeyStoreException {
    String path = null;
    try {
      path = vaultPathBuilder.getPath(alias);
      LOGGER.debug("Writing key to Vault path: '{}'", path);
      final Map<String, Object> keys = new HashMap<>();
      keys.put(VAULT_ENTRY_KEY, key.toString());
      LogicalResponse write = vault.withRetries(maxRetries, retryIntervalMilliseconds)
          .logical()
          .write(path, keys);
      LOGGER.debug("Key written to Vault path: '{}' REST response code: '{}' ", path,
          write.getRestResponse().getStatus());
    } catch (VaultException ex) {
      LOGGER.error("Error writing key to Vault path: '{}' error: '{}' ", path,
          ex);
      throw new KeyStoreException(ex);
    }
  }

}
