package io.scalecube.tokens.store;

/**
 * KeyStore factory class which abstracts the creation of a concrete KeyStore implementation.
 */
public abstract class KeyStoreFactory {
  /**
   * Construct a concrete KeyStore instance.
   * @return a KeyStore
   */
  public static KeyStore get() {
    boolean vaultAddressIsSet = isVaultAddressEnvVarSet();

    if (!vaultAddressIsSet) {
      return new PropertiesFileKeyStore();
    }

    return new VaultKeyStore();
  }

  private static boolean isVaultAddressEnvVarSet() {
    return System.getenv("VAULT_ADDR") != null;
  }
}
