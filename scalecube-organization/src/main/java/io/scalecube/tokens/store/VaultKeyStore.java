package io.scalecube.tokens.store;

import com.bettercloud.vault.EnvironmentLoader;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import io.scalecube.config.AppConfiguration;
import io.scalecube.tokens.KeyStoreException;
import java.util.HashMap;
import java.util.Objects;

/**
 * HshiCorp Vault based KeyStore implementation. Vault access is done via
 * BetterCloud/vault-java-driver at https://github.com/BetterCloud/vault-java-driver.
 * Vault address and token defaults are "VAULT_ADDR" and "VAULT_TOKEN" environment variables
 * respectively. Optional values can be provided in the application settings file.
 */
class VaultKeyStore implements KeyStore {

  @Override
  public void store(String alias, Object key) throws KeyStoreException {
    try {
      final AppConfiguration settings = AppConfiguration.builder().build();
      final Vault vault = new Vault(vaultConfig(settings));
      final String pattern = requiresNonNullOrEmpty(settings, "vault.secret.path");
      final String vaultSecretPath = new EnvironmentLoader()
          .loadVariable("VAULT_SECRETS_PATH");
      final String path = String.format(pattern, vaultSecretPath);
      final HashMap<String, Object> map = new HashMap<>();

      map.put(alias, key);
      vault.logical().write(path, map);
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
      return config;
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

  private static String requiresNonNullOrEmpty(AppConfiguration settings, String key) {
    String input = settings.getProperty(key);
    Objects.requireNonNull(settings.getProperty(key), "missing " + key);
    if (input.length() == 0) {
      throw new IllegalArgumentException("missing " + key);
    }
    return input;
  }

  static boolean isVaultAddressEnvVarSet() {
    String vaultAddress = System.getenv("VAULT_ADDRESS");
    return vaultAddress != null && vaultAddress.length() > 0;
  }
}
