/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.exceptions.AuthorizationException;
import com.databasepreservation.common.client.ViewerConstants;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_DATABASE)
public class SiardResource implements SiardService {
  @Autowired
  private HttpServletRequest request;

  @Override
  public void deleteSIARDFile(String databaseUUID, String siardUUID) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    String path = "";
    try {
      user = controllerAssistant.checkRoles(request);
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      path = database.getPath();
      SIARDController.deleteSIARDFileFromPath(database.getPath(), databaseUUID);
    } catch (GenericException | NotFoundException | AuthorizationException e) {
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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    String result = null;
    String siardPath = "";
    try {
      user = controllerAssistant.checkRoles(request);
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      java.nio.file.Path siardFilesPath = ViewerConfiguration.getInstance().getSIARDFilesPath();
      siardPath = siardFilesPath.resolve(database.getPath()).toString();
      result = getValidationReportPath(validationReportPath, database.getMetadata().getName());
      return SIARDController.validateSIARD(databaseUUID, siardPath, result, allowedTypePath, skipAdditionalChecks);
    } catch (GenericException | NotFoundException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SIARD_PATH_PARAM, siardPath, ViewerConstants.CONTROLLER_REPORT_PATH_PARAM, result,
        ViewerConstants.CONTROLLER_SKIP_ADDITIONAL_CHECKS_PARAM, skipAdditionalChecks);
    }
  }

  @Override
  public ValidationProgressData getValidationProgressData(String databaseUUID, String siardUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      return ValidationProgressData.getInstance(databaseUUID);
    } catch (AuthorizationException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}/download/validation", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads a specific SIARD validation report file from the storage location", description = "")
  public ResponseEntity<Resource> getValidationReportFile(
    @Parameter(name = "The database unique identifier", required = true) @PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    ViewerDatabase database = null;
    try {
      user = controllerAssistant.checkRoles(request);
      database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      File file = new File(database.getValidatorReportPath());
      if (!file.exists()) {
        throw new NotFoundException("validation report file not found");
      }

      InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
      return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
        .contentLength(file.length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    } catch (NotFoundException | GenericException | FileNotFoundException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public void deleteValidationReport(String databaseUUID, String siardUUID, String path) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      SIARDController.deleteValidatorReportFileFromPath(path, databaseUUID);
    } catch (GenericException | AuthorizationException e) {
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
    SIARDUpdateParameters parameters, boolean updateOnModel) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      return SIARDController.updateMetadataInformation(databaseUUID, path, parameters, updateOnModel);
    } catch (GenericException | AuthorizationException e) {
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

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      final ViewerDatabase database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      return database.getMetadata();
    } catch (GenericException | NotFoundException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  private String getValidationReportPath(String validationReportPath, String databaseName) {
    if (validationReportPath == null) {
      String filename = databaseName + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".txt";
      validationReportPath = Paths
        .get(ViewerConfiguration.getInstance().getSIARDReportValidationPath().toString(), filename).toAbsolutePath()
        .toString();
    }

    return validationReportPath;
  }
}
