package io.scalecube.tokens.store;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.config.StringConfigProperty;

final class VaultPathBuilder {

  private StringConfigProperty vaultSecretsPath;
  private StringConfigProperty apiKeysPathPattern;

  VaultPathBuilder() {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();

    vaultSecretsPath = configRegistry.stringProperty("VAULT_SECRETS_PATH");
    apiKeysPathPattern = configRegistry.stringProperty("api.keys.path.pattern");
  }

  String getPath(String alias) {
    return String.format(apiKeysPathPattern.valueOrThrow(), vaultSecretsPath.valueOrThrow())
        .concat(alias);
  }
}
