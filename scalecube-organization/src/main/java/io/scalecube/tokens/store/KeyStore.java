package io.scalecube.tokens.store;

import io.scalecube.tokens.KeyStoreException;

/**
 * Represents an abstraction of key storage.
 */
public interface KeyStore {
  /**
   * Stores the <code>key</code> argument under the <code>alias</code> argument in this KeyStore.
   * @param alias key alias
   * @param key the key to store
   * @throws KeyStoreException in case of an error while saving the key
   */
  void store(String alias, Object key) throws KeyStoreException;
}
