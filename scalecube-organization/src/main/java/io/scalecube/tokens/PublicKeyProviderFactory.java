package io.scalecube.tokens;

import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.config.StringConfigProperty;

/**
 * A factory class used to load the system PublicKeyProvider as it specified in the app settings
 * file. The class entry name is mandatory.
 */
class PublicKeyProviderFactory {

  private static final StringConfigProperty publicKeyProviderClassName =
      ConfigRegistryConfiguration.configRegistry().stringProperty("public.key.provider");
  private static Class<?> publicKeyProvider;

  private PublicKeyProviderFactory() {}

  /**
   * Reads the PublicKeyProvider class name from app settings and return an instance.
   *
   * @return Instance of PublicKeyProvider.
   */
  protected static PublicKeyProvider getPublicKeyProvider() {
    if (publicKeyProvider == null) {
      loadPublicKeyProviderClass();
    }

    return instantiatePublicKeyProvider();
  }

  private static void loadPublicKeyProviderClass() {
    try {
      publicKeyProvider = Class.forName(publicKeyProviderClassName.valueOrThrow());
    } catch (ClassNotFoundException ex) {
      throw new PublicKeyProviderException(ex);
    }
  }

  private static PublicKeyProvider instantiatePublicKeyProvider() {
    try {
      Object obj = publicKeyProvider.newInstance();

      if (obj instanceof PublicKeyProvider) {
        return (PublicKeyProvider) obj;
      }
      throw new PublicKeyProviderException(
          obj.getClass() + " is not assignable from " + PublicKeyProvider.class);
    } catch (InstantiationException | IllegalAccessException ex) {
      throw new PublicKeyProviderException(ex);
    }
  }
}
