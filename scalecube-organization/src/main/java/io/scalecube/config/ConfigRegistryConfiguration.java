package io.scalecube.config;

import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.config.vault.VaultConfigSource;

/**
 * Configures the ConfigRegistry with sources.
 */
public class ConfigRegistryConfiguration {
  private static final int RELOAD_INTERVAL_SEC = 300;
  private static ConfigRegistry configRegistry;

  /**
   * Builds a ConfigRegistry.
   * @return ConfigRegistry
   */
  public static ConfigRegistry configRegistry() {
    if (configRegistry != null) {
      return configRegistry;
    }

    ConfigRegistrySettings.Builder builder = ConfigRegistrySettings.builder()
        .reloadIntervalSec(RELOAD_INTERVAL_SEC)
        .addListener(new Slf4JConfigEventListener())
        .addLastSource("sys_prop", new SystemPropertiesConfigSource())
        .addLastSource("env_var", new SystemEnvironmentConfigSource());

    // for test purposes without vault access
    if (System.getenv().get("VAULT_ADDR") != null) {
      builder.addLastSource("vault", VaultConfigSource.builder().build());
    }

    configRegistry = ConfigRegistry.create(builder.build());
    return configRegistry;
  }
}
