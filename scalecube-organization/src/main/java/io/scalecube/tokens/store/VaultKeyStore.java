package io.scalecube.tokens.store;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import io.scalecube.config.AppConfiguration;
import io.scalecube.tokens.KeyStoreException;
import java.util.HashMap;
import java.util.Map;

/**
 * HshiCorp Vault based KeyStore implementation. Vault access is done via
 * BetterCloud/vault-java-driver at https://github.com/BetterCloud/vault-java-driver.
 * Vault address and token defaults are "VAULT_ADDR" and "VAULT_TOKEN" environment variables
 * respectively. Optional values can be provided in the application settings file.
 */
class VaultKeyStore implements KeyStore {

  private static final String VAULT_ENTRY_KEY = "key";
  private final VaultPathBuilder vaultPathBuilder = new VaultPathBuilder();

  @Override
  public void store(String alias, Object key) throws KeyStoreException {
    try {
      Vault vault = new Vault(vaultConfig(AppConfiguration.builder().build()));
      String path = vaultPathBuilder.getPath(alias);
      final Map<String, Object> keys = new HashMap<>();
      keys.put(VAULT_ENTRY_KEY, key.toString());
      vault.logical().write(path, keys);
    } catch (VaultException ex) {
      throw new KeyStoreException(ex);
    }
  }

  /**
   * Read optional vault address and token from settings file and use them to config vault client.
   * @param settings Application settings
   * @return VaultConfig
   */
  private VaultConfig vaultConfig(AppConfiguration settings) throws VaultException {
    VaultConfig config = new VaultConfig();

    if (isVaultAddressEnvVarSet()) {
      return config.build();
    }

    String address = settings.getProperty("vault.address");
    String token = settings.getProperty("vault.token");

    if (address != null && address.length() > 0) {
      config.address(address);
    }

    if (token != null && token.length() > 0) {
      config.token(token);
    }

    return config.build();
  }


  static boolean isVaultAddressEnvVarSet() {
    String vaultAddress = System.getenv("VAULT_ADDR");
    return vaultAddress != null && vaultAddress.length() > 0;
  }
}
