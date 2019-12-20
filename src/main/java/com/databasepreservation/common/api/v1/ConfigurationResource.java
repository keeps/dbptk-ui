package com.databasepreservation.common.api.v1;

import java.nio.file.Files;

import javax.ws.rs.Path;

import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.databasepreservation.common.client.services.ConfigurationService;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.transformers.DenormalizeSolrStructure;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_CONFIGURATION)
public class ConfigurationResource implements ConfigurationService {
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
        .resolve(tableuuid + "-CURRENT" + ViewerConstants.JSON_EXTENSION);
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
      JsonTransformer.writeObjectToFile(configuration, ViewerConfiguration.getInstance().getDatabaseConfigPath().resolve(databaseuuid)
        .resolve(tableuuid + "-" + configuration.getState() + ViewerConstants.JSON_EXTENSION));
    } catch (ViewerException e) {
      throw new RESTException(e.getMessage());
    }
    return true;
  }

  @Override
  public Boolean denormalize(String databaseuuid) {
    try {
      DenormalizeSolrStructure denormalizeSolrStructure = new DenormalizeSolrStructure(databaseuuid);
    } catch (ModuleException e) {
      throw new RESTException(e.getMessage());
    }
    return true;
  }
}