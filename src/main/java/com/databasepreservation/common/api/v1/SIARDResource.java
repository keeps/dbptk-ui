package com.databasepreservation.common.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.ValidationProgressData;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.models.parameters.CreateSIARDParameters;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.services.SIARDService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_SIARD)
public class SIARDResource implements SIARDService {
  @Context
  private HttpServletRequest request;

  @Override
  public String uploadSIARD(String databaseUUID, String path) throws RESTException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      return SIARDController.loadFromLocal(path, databaseUUID);
    } catch (AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public String uploadMetadataSIARDServer(String path) throws RESTException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      return SIARDController.loadMetadataFromLocal(path);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public String uploadMetadataSIARD(String databaseUUID, String path) throws RESTException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      return SIARDController.loadMetadataFromLocal(databaseUUID, path);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public String findSIARDFile(String path) throws RESTException {
    try {
      return ViewerFactory.getSolrManager().findSIARDFile(path);
    } catch (GenericException | RequestNotValidException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public Boolean createSIARD(String databaseUUID, CreateSIARDParameters parameters) throws RESTException {
    try {
      return SIARDController.createSIARD(databaseUUID, parameters.getConnectionParameters(),
        parameters.getTableAndColumnsParameters(), parameters.getCustomViewsParameters(),
        parameters.getExportOptionsParameters(), parameters.getMetadataExportOptionsParameters());
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public Boolean migrateToDBMS(String databaseUUID, String siardVersion, String siardPath,
    ConnectionParameters connectionParameters) throws RESTException {
    try {
      return SIARDController.migrateToDBMS(databaseUUID, siardVersion, siardPath, connectionParameters);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public Boolean migrateToSIARD(String databaseUUID, String siardVersion, String siardPath,
    CreateSIARDParameters parameters) throws RESTException {
    try {
      return SIARDController.migrateToSIARD(databaseUUID, siardVersion, siardPath,
        parameters.getTableAndColumnsParameters(), parameters.getExportOptionsParameters(),
        parameters.getMetadataExportOptionsParameters());
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public ViewerMetadata updateMetadataInformation(String databaseUUID, String path, SIARDUpdateParameters parameters)
    throws RESTException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      return SIARDController.updateMetadataInformation(databaseUUID, path, parameters);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public Boolean validateSIARD(String databaseUUID, String SIARDPath, String validationReportPath,
    String allowedTypePath, boolean skipAdditionalChecks) throws RESTException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      return SIARDController.validateSIARD(databaseUUID, SIARDPath, validationReportPath, allowedTypePath,
        skipAdditionalChecks);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, SIARDPath, ViewerConstants.CONTROLLER_REPORT_PATH_PARAM,
        validationReportPath, ViewerConstants.CONTROLLER_SKIP_ADDITIONAL_CHECKS_PARAM, skipAdditionalChecks);
    }
  }

  @Override
  public ValidationProgressData getValidationProgressData(String databaseUUID) {
    return ValidationProgressData.getInstance(databaseUUID);
  }

  @Override
  public void clearValidationProgressData(String databaseUUID) {
    ValidationProgressData.clear(databaseUUID);
  }

  @Override
  public void updateStatusValidate(String databaseUUID, ViewerDatabaseValidationStatus status) {
    SIARDController.updateStatusValidate(databaseUUID, status);
  }

  @Override
  public void deleteSIARDFile(String databaseUUID, String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      SIARDController.deleteSIARDFileFromPath(path, databaseUUID);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      e.printStackTrace();
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public void deleteSIARDValidatorReportFile(String databaseUUID, String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(UserUtility.getUser(request));
      SIARDController.deleteValidatorReportFileFromPath(path, databaseUUID);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      e.printStackTrace();
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_REPORT_PATH_PARAM, path);
    }
  }

}
