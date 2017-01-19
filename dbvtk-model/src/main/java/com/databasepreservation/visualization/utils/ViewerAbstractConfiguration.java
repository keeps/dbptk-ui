package com.databasepreservation.visualization.utils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class ViewerAbstractConfiguration {
  private final Configuration configuration;

  public ViewerAbstractConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /*
   * Implementation-dependent parts
   * ____________________________________________________________________________________________________________________
   */
  public abstract void clearViewerCachableObjectsAfterConfigurationChange();

  public abstract Path getLobPath();

  /*
   * Base property retrieval methods
   * ____________________________________________________________________________________________________________________
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  public String getViewerConfigurationAsString(String... keyParts) {
    return configuration.getString(getConfigurationKey(keyParts));
  }

  public int getViewerConfigurationAsInt(int defaultValue, String... keyParts) {
    return configuration.getInt(getConfigurationKey(keyParts), defaultValue);
  }

  public int getViewerConfigurationAsInt(String... keyParts) {
    return getViewerConfigurationAsInt(0, keyParts);
  }

  public boolean getViewerConfigurationAsBoolean(boolean defaultValue, String... keyParts) {
    return configuration.getBoolean(getConfigurationKey(keyParts), defaultValue);
  }

  public boolean getViewerConfigurationAsBoolean(String... keyParts) {
    return getViewerConfigurationAsBoolean(false, keyParts);
  }

  public List<String> getViewerConfigurationAsList(String... keyParts) {
    String[] array = configuration.getStringArray(getConfigurationKey(keyParts));
    return Arrays.asList(array).stream().filter(v -> StringUtils.isNotBlank(v)).collect(Collectors.toList());
  }

  /*
   * "Internal" helper methods
   * ____________________________________________________________________________________________________________________
   */
  private static String getConfigurationKey(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return sb.toString();
  }
}
