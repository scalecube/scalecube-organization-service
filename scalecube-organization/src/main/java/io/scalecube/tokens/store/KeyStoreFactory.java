package io.scalecube.tokens.store;

import static io.scalecube.tokens.store.VaultKeyStore.isVaultAddressEnvVarSet;

public abstract class KeyStoreFactory {
  public static KeyStore get() {
    boolean vaultAddressIsSet = isVaultAddressEnvVarSet();

    if (!vaultAddressIsSet) {
      return new PropertiesFileKeyStore();
    }

    return new VaultKeyStore();
  }
}
