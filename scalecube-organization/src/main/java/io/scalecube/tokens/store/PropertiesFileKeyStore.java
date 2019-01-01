package io.scalecube.tokens.store;

import io.scalecube.tokens.KeyStoreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

class PropertiesFileKeyStore implements KeyStore {

  File parentPath;

  public PropertiesFileKeyStore() {
    Iterator<String> rootDir = rootPaths.iterator();
    while (rootDir.hasNext()) {
      try {
        File path = new File(rootDir.next());
        if (!path.isDirectory()) {
          path.delete();
          path.mkdirs();
          if (path.canWrite()) {
            parentPath = path;
            return;
          }
        }
      } catch (SecurityException ignoredException) {
        continue;
      }
    }
  }

  @Override
  public void store(String alias, Object key) throws KeyStoreException {
    Properties properties = new Properties();
    try {

      File keystore = new File(parentPath, "keystore.properties");
      if (keystore.exists()) {
        properties.load(new FileInputStream(keystore));
      }
      properties.put(alias, key);
      properties.store(new FileOutputStream(keystore), "");
    } catch (IOException ex) {
      throw new KeyStoreException(ex);
    }
  }

  private static final List<String> rootPaths = Arrays.asList("/etc/opt/om2", "/tmp/opt/om2");
}
