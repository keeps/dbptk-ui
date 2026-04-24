/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.configuration.migration;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationMigratorUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMigratorUtils.class);
  private static final String FALLBACK_VERSION = "1.0.0";
  public static final String VERSION_KEY = "version";

  public static boolean isFileOutdated(Path filePath, String targetSystemVersion) throws IOException {
    ObjectMapper tempMapper = new ObjectMapper();
    JsonNode rootNode = tempMapper.readTree(filePath.toFile());

    JsonNode versionNode = rootNode.get(VERSION_KEY);
    String currentVersion = (versionNode != null && !versionNode.isNull()) ? versionNode.asText() : FALLBACK_VERSION;

    return isOlderThan(currentVersion, targetSystemVersion);
  }

  public static boolean isOlderThan(String currentVersion, String targetVersion) {
    try {
      ComparableVersion current = new ComparableVersion(currentVersion);
      ComparableVersion target = new ComparableVersion(targetVersion);
      return current.compareTo(target) < 0;
    } catch (Exception e) {
      LOGGER.warn("Invalid version format: '{}'. Assuming it's older than '{}'.", currentVersion, targetVersion);
      return true;
    }
  }

  public static String extractVersion(JsonNode node) {
    JsonNode versionNode = node.get(VERSION_KEY);
    return (versionNode != null && !versionNode.isNull()) ? versionNode.asText() : FALLBACK_VERSION;
  }
}
