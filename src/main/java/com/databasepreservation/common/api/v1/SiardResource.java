package com.databasepreservation.common.api.v1;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.progress.ValidationProgressData;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.SiardService;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

import io.swagger.annotations.ApiOperation;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_DATABASE)
public class SiardResource implements SiardService {
  @Context
  private HttpServletRequest request;

  @Override
  public void deleteSIARDFile(String databaseUUID, String siardUUID) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    String path = "";
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      path = database.getPath();
      SIARDController.deleteSIARDFileFromPath(database.getPath(), databaseUUID);
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public ViewerDatabase getSiard(String databaseUUID, String siardUUID) {
    return new ViewerDatabase();
  }

  /*******************************************************************************
   * Validation Sub-resource
   ******************************************************************************/
  @Override
  public Boolean validateSiard(String databaseUUID, String siardUUID, String validationReportPath,
    String allowedTypePath, boolean skipAdditionalChecks) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    String result = null;
    String path = "";
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      java.nio.file.Path siardFilesPath = ViewerConfiguration.getInstance().getSIARDFilesPath();
      java.nio.file.Path siardPath = siardFilesPath.resolve(database.getPath());
      path = database.getPath();
      result = getValidationReportPath(validationReportPath, siardPath);
      return SIARDController.validateSIARD(databaseUUID, siardPath.toAbsolutePath().toString(), result, allowedTypePath,
        skipAdditionalChecks);
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path, ViewerConstants.CONTROLLER_REPORT_PATH_PARAM, result,
        ViewerConstants.CONTROLLER_SKIP_ADDITIONAL_CHECKS_PARAM, skipAdditionalChecks);
    }
  }

  @Override
  public ValidationProgressData getValidationProgressData(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ValidationProgressData.getInstance(databaseUUID);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}/validation")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads a specific SIARD validation report file from the storage location", notes = "")
  public Response getValidationReportFile(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    ViewerDatabase database = null;
    try {
      database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      File file = new File(database.getValidatorReportPath());
      if (!file.exists()) {
        throw new RESTException(new NotFoundException("validation report file not found"));
      }

      Response.ResponseBuilder responseBuilder = Response.ok(file);
      responseBuilder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      return responseBuilder.build();
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public void deleteValidationReport(String databaseUUID, String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      SIARDController.deleteValidatorReportFileFromPath(path, databaseUUID);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_REPORT_PATH_PARAM, path);
    }
  }

  /*******************************************************************************
   * Metadata Sub-resource
   ******************************************************************************/
  @Override
  public ViewerMetadata updateMetadataInformation(String databaseUUID, String siardUUID, String path,
    SIARDUpdateParameters parameters) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      return SIARDController.updateMetadataInformation(databaseUUID, path, parameters);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, path);
    }
  }

  @Override
  public ViewerMetadata getMetadataInformation(String databaseUUID, String siardUUID) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);
    try {
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      return database.getMetadata();
    } catch (GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  private String getValidationReportPath(String validationReportPath, java.nio.file.Path siardPath) {
    if (validationReportPath == null) {
      String filename = siardPath.toString().replaceFirst("[.][^.]+$", "") + "-"
        + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".txt";
      validationReportPath = Paths
        .get(ViewerConfiguration.getInstance().getSIARDReportValidationPath().toString(), filename).toAbsolutePath()
        .toString();
    }

    return validationReportPath;
  }
}
