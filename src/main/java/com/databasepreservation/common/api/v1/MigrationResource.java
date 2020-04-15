package com.databasepreservation.common.api.v1;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.models.wizard.CreateSIARDParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionResponse;
import com.databasepreservation.common.client.services.MigrationService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_MIGRATION)
public class MigrationResource implements MigrationService {
  @Context
  private HttpServletRequest request;

  @Override
  public List<Module> getSiardModules(String type, String moduleName) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      if (moduleName != null) {
        if (type == null) {
          return SIARDController.getSIARDModules(moduleName);
        } else if (type.equals("import")) {
          return SIARDController.getSIARDImportModule(moduleName);
        } else if (type.equals("export")) {
          return SIARDController.getSIARDExportModule(moduleName);
        } else {
          // type must be import or export
          return Collections.emptyList();
        }
      } else {
        if (type == null) {
          return SIARDController.getSiardModules();
        } else if (type.equals("import")) {
          return SIARDController.getSIARDImportModules();
        } else if (type.equals("export")) {
          return SIARDController.getSIARDExportModules();
        } else {
          // type must be import or export
          return Collections.emptyList();
        }
      }
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public List<Module> getDBMSModules(String type, String moduleName) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      if (moduleName != null) {
        if (type == null) {
          return SIARDController.getDBMSModules(moduleName);
        } else if (type.equals("import")) {
          return SIARDController.getDatabaseImportModule(moduleName);
        } else if (type.equals("export")) {
          return SIARDController.getDatabaseExportModule(moduleName);
        } else {
          // type must be import or export
          return Collections.emptyList();
        }
      } else {
        if (type == null) {
          return SIARDController.getDBMSModules();
        } else if (type.equals("import")) {
          return SIARDController.getDatabaseImportModules();
        } else if (type.equals("export")) {
          return SIARDController.getDatabaseExportModules();
        } else {
          // type must be import or export
          return Collections.emptyList();
        }
      }
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public List<Module> getFilterModules(String moduleName) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      if (moduleName != null) {
        return SIARDController.getDatabaseFilterModule(moduleName);
      } else {
        return SIARDController.getDatabaseFilterModules();
      }
    } finally {
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public ConnectionResponse testConnection(ConnectionParameters connectionParameters) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return SIARDController.testConnection(connectionParameters);
    } finally {
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public List<List<String>> testQuery(ConnectionParameters parameters, String query) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return SIARDController.validateCustomViewQuery(parameters, query);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public ViewerMetadata getMetadata(ConnectionParameters connectionParameters) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return SIARDController.getDatabaseMetadata(connectionParameters);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public String run(String databaseUUID, CreateSIARDParameters parameters) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      if (databaseUUID != null) {
        final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);

        if (parameters.getConnectionParameters() != null) {
          SIARDController.migrateToDBMS(databaseUUID, database.getVersion(), database.getPath(),
            parameters.getConnectionParameters());
        } else {
          SIARDController.migrateToSIARD(databaseUUID, database.getVersion(), database.getPath(),
            parameters.getTableAndColumnsParameters(), parameters.getExportOptionsParameters(),
            parameters.getMetadataExportOptionsParameters());
        }
        return databaseUUID;
      } else {
        return SIARDController.createSIARD(parameters.getUniqueID(), parameters.getConnectionParameters(),
          parameters.getTableAndColumnsParameters(), parameters.getCustomViewsParameters(), parameters.getMerkleTreeFilterParameters(),
          parameters.getExportOptionsParameters(), parameters.getMetadataExportOptionsParameters());
      }
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }
}
