package io.scalecube.tokens.store;

import com.bettercloud.vault.EnvironmentLoader;
import io.scalecube.config.AppConfiguration;
import java.util.Objects;

final class VaultPathBuilder {
  private String vaultTokenKeysPath;

  VaultPathBuilder() {
    String vaultSecretPathPrefix = new EnvironmentLoader()
        .loadVariable("VAULT_SECRETS_PATH");
    String pattern = getVaultPathPattern();
    vaultTokenKeysPath = String.format(pattern, vaultSecretPathPrefix);
  }

  String getPath(String alias) {
    return getVaultSecretPath().concat(alias);
  }

  private String getVaultSecretPath() {
    if (vaultTokenKeysPath == null) {
      vaultTokenKeysPath = String.format(getVaultPathPattern(), getVaultKeyValueEngine());
    }
    return vaultTokenKeysPath;
  }

  private static String getVaultPathPattern() {
    final AppConfiguration settings = AppConfiguration.builder().build();
    String vaultSecretPath = settings.getProperty("vault.secret.path");
    Objects.requireNonNull(vaultSecretPath, "missing vault.secret.path");
    return vaultSecretPath;
  }

  private String getVaultKeyValueEngine() {
    EnvironmentLoader environmentLoader = new EnvironmentLoader();
    String vaultKeyValueEngine = environmentLoader.loadVariable("VAULT_SECRETS_PATH");
    Objects.requireNonNull(vaultKeyValueEngine,  "missing 'VAULT_SECRETS_PATH' env variable");
    return vaultKeyValueEngine;
  }
}
