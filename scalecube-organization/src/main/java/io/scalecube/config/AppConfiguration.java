package io.scalecube.config;

import java.io.IOException;
import java.util.Properties;

public class AppConfiguration {

  private final Properties settings;

  private AppConfiguration() {
    settings = new Properties();

    try {
      settings.load(getClass().getResourceAsStream("/settings.properties"));
    } catch (IOException ex) {
      throw new AppConfigurationException("Failed to initialize", ex);
    }
  }

  public static Builder builder() {
    return new AppConfiguration.Builder();
  }

  public String getProperty(String key) {
    return settings.getProperty(key);
  }

  public static class Builder {
    public AppConfiguration build() {
      return new AppConfiguration();
    }
  }
}