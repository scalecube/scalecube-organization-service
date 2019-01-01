package io.scalecube.tokens.store;

import io.scalecube.tokens.KeyStoreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class PropertiesFileKeyStore implements KeyStore {

  @Override
  public void store(String alias, Object key) throws KeyStoreException {
    Properties properties = new Properties();
    try {
      if (new File("keystore.properties").exists()) {
        properties.load(new FileInputStream("keystore.properties"));
      }
      properties.put(alias, key);
      properties.store(new FileOutputStream("keystore.properties"), "");
    } catch (IOException ex) {
      throw new KeyStoreException(ex);
    }
  }
}
