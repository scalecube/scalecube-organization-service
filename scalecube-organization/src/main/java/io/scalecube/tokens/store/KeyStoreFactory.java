package io.scalecube.tokens.store;

import static io.scalecube.tokens.store.VaultKeyStore.isVaultAddressEnvVarSet;

/**
 * KeyStore factory class which abstracts the creation of a concrete KeyStore implementation.
 */
public abstract class KeyStoreFactory {
  /**
   * Construct a concrete KeyStore object.
   * @return an instance of KeyStore
   */
  public static KeyStore get() {
    boolean vaultAddressIsSet = isVaultAddressEnvVarSet();

    if (!vaultAddressIsSet) {
      return new PropertiesFileKeyStore();
    }

    return new VaultKeyStore();
  }
}
