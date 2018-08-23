package io.scalecube.tokens;

public abstract class KeyStoreFactory {
  public static KeyStore get() {
    return new PropertiesFileKeyStore();
  }
}
