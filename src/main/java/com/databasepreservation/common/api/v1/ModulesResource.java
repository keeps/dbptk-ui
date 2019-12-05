package com.databasepreservation.common.api.v1;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.ConnectionResponse;
import com.databasepreservation.common.client.models.DBPTKModule;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.services.ModulesService;
import com.databasepreservation.common.server.controller.SIARDController;
import org.roda.core.data.exceptions.GenericException;
import org.springframework.stereotype.Service;

import javax.ws.rs.Path;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_MODULES)
public class ModulesResource implements ModulesService {

  @Override
  public DBPTKModule getImportDBPTKModules() {
    try {
      return SIARDController.getDatabaseImportModules();
    } catch (GenericException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public DBPTKModule getExportDBPTKModules() {
    try {
      return SIARDController.getDatabaseExportModules();
    } catch (GenericException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public DBPTKModule getSIARDExportModule(String moduleName) {
    try {
      return SIARDController.getSIARDExportModule(moduleName);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public DBPTKModule getSIARDExportModules() {
    try {
      return SIARDController.getSIARDExportModules();
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public ConnectionResponse testDBConnection(final ConnectionParameters connectionParameters) {
      return SIARDController.testConnection(connectionParameters);
  }
}
