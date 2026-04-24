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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.configuration.migration.VersionMigrator.MigrationType;
import com.databasepreservation.common.server.configuration.migration.v1Tov2.CollectionMigratorV1ToV2;
import com.databasepreservation.common.server.configuration.migration.v1Tov2.DatabaseMigratorV1ToV2;
import com.databasepreservation.common.server.configuration.migration.v1Tov2.DenormalizeMigratorV1ToV2;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationMigrator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMigrator.class);

  private final ObjectMapper mapper;
  private final ViewerDatabase database;
  private final List<VersionMigrator> pipeline;

  public ConfigurationMigrator(ViewerDatabase database) {
    this.mapper = new ObjectMapper();
    this.database = database;
    // Add migrations in the order they should be applied
    this.pipeline = new ArrayList<>();

    // Database migrators
    this.pipeline.add(new DatabaseMigratorV1ToV2());

    // Collection migrators
    this.pipeline.add(new CollectionMigratorV1ToV2());

    // Denormalization migrators
    this.pipeline.add(new DenormalizeMigratorV1ToV2());
  }

  public void migrateDatabaseStatus(Path filePath) throws IOException {
    runPipeline(filePath, ViewerConstants.DATABASE_STATUS_VERSION, MigrationType.DATABASE);
  }

  public void migrateCollectionStatus(Path filePath) throws IOException {
    runPipeline(filePath, ViewerConstants.COLLECTION_STATUS_VERSION, MigrationType.COLLECTION);
  }

  public void migrateDenormalizeConfiguration(Path filePath) throws IOException {
    runPipeline(filePath, ViewerConstants.DENORMALIZATION_STATUS_VERSION, MigrationType.DENORMALIZE);
  }

  private void runPipeline(Path filePath, String targetSystemVersion, MigrationType type) throws IOException {
    JsonNode rootNode = mapper.readTree(filePath.toFile());
    String currentVersion = ConfigurationMigratorUtils.extractVersion(rootNode);

    if (!ConfigurationMigratorUtils.isOlderThan(currentVersion, targetSystemVersion)) {
      return;
    }

    ObjectNode node = (ObjectNode) rootNode;
    boolean modified = false;

    for (VersionMigrator migrator : pipeline) {
      if (migrator.getType() == type && currentVersion.equals(migrator.getSourceVersion())
        && ConfigurationMigratorUtils.isOlderThan(currentVersion, targetSystemVersion)) {

        node = migrator.migrate(node, this.database);
        currentVersion = migrator.getTargetVersion();
        node.put(ConfigurationMigratorUtils.VERSION_KEY, currentVersion);
        modified = true;
      }
    }

    if (modified) {
      mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), node);
      LOGGER.info("File {} migrated to version {}", filePath.getFileName(), currentVersion);
    }
  }

}