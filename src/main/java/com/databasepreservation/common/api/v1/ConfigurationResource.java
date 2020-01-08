package com.databasepreservation.common.api.v1;

import java.nio.file.Files;

import javax.ws.rs.Path;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.services.ConfigurationService;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.JsonTransformer;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_CONFIGURATION)
public class ConfigurationResource implements ConfigurationService {

  @Override
  public CollectionStatus getCollectionStatus(String databaseUUID, String collectionUUID) {
    try {
      final java.nio.file.Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
      final java.nio.file.Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);
      final java.nio.file.Path collectionStatusFile = databaseDirectoryPath.resolve(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + collectionUUID + ".json");
      return JsonUtils.readObjectFromFile(collectionStatusFile, CollectionStatus.class);
    } catch (GenericException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Boolean createConfigurationFile(String databaseuuid, CollectionConfiguration configuration) {
    try {
      JsonTransformer.writeObjectToFile(configuration, ViewerConfiguration.getInstance().getDatabaseConfigPath()
        .resolve(databaseuuid).resolve(databaseuuid + ViewerConstants.JSON_EXTENSION));
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
    return true;
  }

  @Override
  public CollectionConfiguration getConfigurationFile(String databaseuuid) {
    try {
      java.nio.file.Path path = ViewerConfiguration.getInstance().getDatabaseConfigPath().resolve(databaseuuid)
        .resolve(databaseuuid + ViewerConstants.JSON_EXTENSION);
      if (Files.exists(path)) {
        return JsonTransformer.readObjectFromFile(path, CollectionConfiguration.class);
      } else {
        return null;
      }
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public DenormalizeConfiguration getDenormalizeConfigurationFile(String databaseuuid, String tableuuid) {
    try {
      java.nio.file.Path path = ViewerConfiguration.getInstance().getDatabaseConfigPath().resolve(databaseuuid)
        .resolve(tableuuid + ViewerConstants.JSON_EXTENSION);
      if (Files.exists(path)) {
        return JsonTransformer.readObjectFromFile(path, DenormalizeConfiguration.class);
      } else {
        return null;
      }
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public Boolean createDenormalizeConfigurationFile(String databaseuuid, String tableuuid,
    DenormalizeConfiguration configuration) {
    try {
      JsonTransformer.writeObjectToFile(configuration, ViewerConfiguration.getInstance().getDatabaseConfigPath()
        .resolve(databaseuuid).resolve(tableuuid + ViewerConstants.JSON_EXTENSION));
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
    return true;
  }

  @Override
  public CollectionConfiguration getConfiguration(String databaseuuid) {
    try {
      java.nio.file.Path path = ViewerConfiguration.getInstance().getDatabaseConfigPath().resolve(databaseuuid)
        .resolve(databaseuuid + ViewerConstants.JSON_EXTENSION);

      if (Files.exists(path)) {
        CollectionConfiguration collectionConfiguration = JsonTransformer.readObjectFromFile(path,
          CollectionConfiguration.class);

        for (TableConfiguration table : collectionConfiguration.getTables()) {
          table.setDenormalizeConfiguration(getDenormalizeConfigurationFile(databaseuuid, table.getUuid()));
        }
        return JsonTransformer.readObjectFromFile(path, CollectionConfiguration.class);
      } else {
        return null;
      }
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public Boolean createConfigurationBundle(String databaseuuid, CollectionConfiguration configuration) {
    try {
      for (TableConfiguration table : configuration.getTables()) {
        if (table.getDenormalizeConfiguration().getRelatedTables() != null
          && !table.getDenormalizeConfiguration().getRelatedTables().isEmpty()) {
          JsonTransformer.writeObjectToFile(table.getDenormalizeConfiguration(), ViewerConfiguration.getInstance()
            .getDatabaseConfigPath().resolve(databaseuuid).resolve(table.getUuid() + ViewerConstants.JSON_EXTENSION));
        }
      }
      JsonTransformer.writeObjectToFile(configuration, ViewerConfiguration.getInstance().getDatabaseConfigPath()
        .resolve(databaseuuid).resolve(databaseuuid + ViewerConstants.JSON_EXTENSION));

    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
    return true;
  }

  @Override
  public Boolean updateDenormalizeConfiguration(String databaseuuid, String tableuuid, ViewerJobStatus status) {
    DenormalizeConfiguration configuration = getDenormalizeConfigurationFile(databaseuuid, tableuuid);
    configuration.setState(status);
    try {
      JsonTransformer.writeObjectToFile(configuration, ViewerConfiguration.getInstance().getDatabaseConfigPath()
        .resolve(databaseuuid).resolve(tableuuid + ViewerConstants.JSON_EXTENSION));
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
    return true;
  }
}
