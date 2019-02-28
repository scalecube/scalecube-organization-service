package io.scalecube.organization.tokens.store;

import io.scalecube.organization.tokens.KeyStoreException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/** Represents an abstraction of key storage. */
public interface KeyStore {

  /**
   * Stores the <code>key</code> argument under the <code>alias</code> argument in this KeyStore.
   *
   * @param alias key alias
   * @param keyPair the key pair to store
   * @throws KeyStoreException in case of an error while saving the key
   */
  void store(String alias, KeyPair keyPair);

  PublicKey getPublicKey(String keyId);

  PrivateKey getPrivateKey(String keyId);
}
