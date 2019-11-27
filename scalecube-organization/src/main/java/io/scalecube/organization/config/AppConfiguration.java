package io.scalecube.organization.config;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.ConfigRegistrySettings.Builder;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.config.vault.EnvironmentVaultTokenSupplier;
import io.scalecube.config.vault.KubernetesVaultTokenSupplier;
import io.scalecube.config.vault.VaultConfigSource;
import io.scalecube.config.vault.VaultInvoker;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Configures the ConfigRegistry with sources. */
public class AppConfiguration {

  private static final String VAULT_ADDR_PROP_NAME = "VAULT_ADDR";
  private static final String VAULT_TOKEN_PROP_NAME = "VAULT_TOKEN";
  private static final String KUBERNETES_VAULT_ROLE_PROP_NAME = "VAULT_ROLE";
  private static final String VAULT_SECRETS_PATH_PROP_NAME = "VAULT_SECRETS_PATH";
  private static final String VAULT_ENGINE_VERSION_PROP_NAME = "VAULT_ENGINE_VERSION";

  private static final int RELOAD_INTERVAL_SEC = 300;
  private static final Pattern CONFIG_PATTERN = Pattern.compile("(.*)\\.config\\.properties");
  private static final Predicate<Path> PATH_PREDICATE =
      path -> CONFIG_PATTERN.matcher(path.toString()).matches();

  private static final ConfigRegistry configRegistry;
  private static VaultInvoker vaultInvoker;

  static {
    Builder builder =
        ConfigRegistrySettings.builder()
            .reloadIntervalSec(RELOAD_INTERVAL_SEC)
            .addListener(new Slf4JConfigEventListener());

    String vaultAddr = System.getenv().get(VAULT_ADDR_PROP_NAME);
    String secretsPath = System.getenv().get(VAULT_SECRETS_PATH_PROP_NAME);
    int vaultEngineVersion =
        Integer.parseInt(System.getenv().getOrDefault(VAULT_ENGINE_VERSION_PROP_NAME, "1"));
    // for test purposes without vault access
    if (vaultAddr != null && secretsPath != null) {
      String vaultToken = System.getenv().get(VAULT_TOKEN_PROP_NAME);
      String kubernetesVaultRolePropName = System.getenv().get(KUBERNETES_VAULT_ROLE_PROP_NAME);
      if (vaultToken == null && kubernetesVaultRolePropName == null) {
        throw new IllegalArgumentException("Vault auth scheme is required");
      }
      if (vaultToken != null && kubernetesVaultRolePropName != null) {
        throw new IllegalArgumentException("Vault auth scheme is unclear");
      }
      VaultInvoker.Builder vaultInvokerBuilder = VaultInvoker.builder();

      if (vaultToken != null) {
        vaultInvokerBuilder.tokenSupplier(new EnvironmentVaultTokenSupplier());
      }
      if (kubernetesVaultRolePropName != null) {
        vaultInvokerBuilder.tokenSupplier(new KubernetesVaultTokenSupplier());
      }
      vaultInvoker = vaultInvokerBuilder.options(c -> c.engineVersion(vaultEngineVersion)).build();

      builder.addLastSource(
          "vault",
          VaultConfigSource.builder().secretsPath(secretsPath).invoker(vaultInvoker).build());
    }
    builder.addLastSource("sys_prop", new SystemPropertiesConfigSource());
    builder.addLastSource("env_var", new SystemEnvironmentConfigSource());
    builder.addLastSource("cp", new ClassPathConfigSource(PATH_PREDICATE));

    configRegistry = ConfigRegistry.create(builder.build());
  }

  public static ConfigRegistry configRegistry() {
    return configRegistry;
  }

  public static VaultInvoker vaultInvoker() {
    return vaultInvoker;
  }
}
