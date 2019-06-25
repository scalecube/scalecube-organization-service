package io.scalecube.organization.fixtures;

import io.scalecube.organization.tokens.KeyStoreException;
import io.scalecube.organization.tokens.store.KeyStore;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InMemoryKeyStore implements KeyStore {

  private Map<String, String> storage = new HashMap<>();

  @Override
  public void store(String alias, KeyPair keyPair) {
    storage.put(alias + "-public", encodeKey(keyPair.getPublic()));
    storage.put(alias + "-private", encodeKey(keyPair.getPrivate()));
  }

  @Override
  public PublicKey getPublicKey(String keyId) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      byte[] encodedKey = Base64.getDecoder().decode(storage.get(keyId + "-public").getBytes());

      return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    } catch (GeneralSecurityException ex) {
      throw new KeyStoreException(ex);
    }
  }

  @Override
  public PrivateKey getPrivateKey(String keyId) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      byte[] encodedKey = Base64.getDecoder().decode(storage.get(keyId + "-private").getBytes());

      return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    } catch (GeneralSecurityException ex) {
      throw new KeyStoreException(ex);
    }
  }

  @Override
  public void delete(String keyId) {
    storage.remove(storage.get(keyId + "-public"));
    storage.remove(storage.get(keyId + "-private"));
  }

  private String encodeKey(Key key) {
    return new String(Base64.getEncoder().encode(key.getEncoded()));
  }
}
