package io.scalecube.organization.token.store;

import io.scalecube.tokens.KeyStoreException;
import io.scalecube.tokens.store.KeyStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

public class PropertiesFileKeyStore implements KeyStore {

  private Properties properties = new Properties();

  public PropertiesFileKeyStore() {
    try {
      if (new File("keystore.properties").exists()) {
        properties.load(new FileInputStream("keystore.properties"));
      }
    } catch (IOException ex) {
      throw new KeyStoreException(ex);
    }
  }

  @Override
  public void store(String alias, KeyPair keyPair) {
    properties.put(alias + "-public", encodeKey(keyPair.getPublic()));
    properties.put(alias + "-private", encodeKey(keyPair.getPrivate()));

    try {
      properties.store(new FileOutputStream("keystore.properties"), "");
    } catch (IOException ex) {
      throw new KeyStoreException(ex);
    }
  }

  @Override
  public PublicKey getPublicKey(String keyId) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      byte[] encodedKey =
          Base64.getDecoder().decode(properties.getProperty(keyId + "-public").getBytes());

      return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    } catch (GeneralSecurityException ex) {
      throw new KeyStoreException(ex);
    }
  }

  @Override
  public PrivateKey getPrivateKey(String keyId) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      byte[] encodedKey =
          Base64.getDecoder().decode(properties.getProperty(keyId + "-private").getBytes());

      return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    } catch (GeneralSecurityException ex) {
      throw new KeyStoreException(ex);
    }
  }

  private String encodeKey(Key key) {
    return new String(Base64.getEncoder().encode(key.getEncoded()));
  }
}
