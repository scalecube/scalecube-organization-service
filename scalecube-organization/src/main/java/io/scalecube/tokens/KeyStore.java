package io.scalecube.tokens;

/**
 * Represents an abstraction of key storage.
 */
public interface KeyStore {

  /**
   * Stores the <code>key</code> argument under the <code>alias</code> argument in this KeyStore
   * @param alias
   * @param key
   * @throws KeyStoreException
   */
  void store(String alias, Object key) throws KeyStoreException;
}
