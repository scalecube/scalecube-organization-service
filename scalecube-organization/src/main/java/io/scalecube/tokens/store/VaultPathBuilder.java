package io.scalecube.tokens.store;

import com.bettercloud.vault.EnvironmentLoader;
import io.scalecube.config.AppConfiguration;
import java.util.Objects;

final class VaultPathBuilder {
  private String vaultTokenKeysPath;

  VaultPathBuilder() {
    EnvironmentLoader environmentLoader = new EnvironmentLoader();
    AppConfiguration settings = AppConfiguration.builder().build();

    String vaultSecretPathPrefix =
        Objects.requireNonNull(
            environmentLoader.loadVariable("VAULT_SECRETS_PATH"),
            "missing 'VAULT_SECRETS_PATH' env variable");
    String vaultPathPattern =
        Objects.requireNonNull(
            settings.getProperty("vault.secret.path"), "missing vault.secret.path");

    vaultTokenKeysPath = String.format(vaultPathPattern, vaultSecretPathPrefix);
  }

  String getPath(String alias) {
    return vaultTokenKeysPath.concat(alias);
  }
}
