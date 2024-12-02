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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.databasepreservation.common.utils.LobManagerUtils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.exceptions.AuthorizationException;
import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.FileService;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.controller.Browser;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.google.common.io.Files;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_FILE)
public class FileResource implements FileService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileResource.class);
  @Autowired
  private HttpServletRequest request;

  @Override
  public List<String> list() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      final java.nio.file.Path path = ViewerConfiguration.getInstance().getSIARDFilesPath();
      return java.nio.file.Files.walk(path).filter(java.nio.file.Files::isRegularFile).sorted(Comparator.naturalOrder())
        .map(java.nio.file.Path::getFileName).map(java.nio.file.Path::toString).collect(Collectors.toList());
    } catch (IOException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public ResponseEntity<Resource> getSIARDFile(String filename) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      java.nio.file.Path siardFilesPath = ViewerConfiguration.getInstance().getSIARDFilesPath();
      java.nio.file.Path basePath = Paths.get(ViewerConfiguration.getInstance().getViewerConfigurationAsString(siardFilesPath.toString(),
        ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH));
      java.nio.file.Path siardPath = siardFilesPath.resolve(filename);

      if (java.nio.file.Files.isDirectory(siardPath)) {
        siardPath = LobManagerUtils.zipDirectory(siardPath);
      }

      if (java.nio.file.Files.exists(siardPath) && (ViewerConfiguration.checkPathIsWithin(siardPath, siardFilesPath)
        || ViewerConfiguration.checkPathIsWithin(siardPath, basePath))) {

        InputStreamResource resource = new InputStreamResource(new FileInputStream(siardPath.toFile()));
        return ResponseEntity.ok()
          .header("Content-Disposition", "attachment; filename=\"" + siardPath.toFile().getName() + "\"")
          .contentLength(siardPath.toFile().length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
      } else {
        throw new NotFoundException("SIARD file not found");
      }
    } catch (NotFoundException | AuthorizationException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  @Override
  public void deleteSiardFile(String filename) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      java.nio.file.Files.walk(ViewerConfiguration.getInstance().getSIARDFilesPath()).map(java.nio.file.Path::toFile)
        .filter(p -> p.getName().equals(filename)).forEach(File::delete);
      LOGGER.info("SIARD file removed from system ({})", filename);
    } catch (IOException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(new NotFoundException("Could not delete SIARD file: " + filename + " from the system"));
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  @Override
  public ResponseEntity<ApiResponseMessage> createSIARDFile(MultipartFile resource, String acceptFormat) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();
    String filename = "";

    // delegate action to controller
    try {
      user = controllerAssistant.checkRoles(request);

      String mediaType = ApiUtils.getMediaType(acceptFormat, request);
      filename = resource.getOriginalFilename();
      String fileExtension = Files.getFileExtension(filename);

      if (!fileExtension.equals(ViewerConstants.SIARD)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponseMessage(ApiResponseMessage.ERROR, "Must be a SIARD file"));
      }

      java.nio.file.Path path = Paths.get(ViewerConfiguration.getInstance().getSIARDFilesPath().toString(), filename);
      Browser.createFile(resource.getInputStream(), filename, path);
      return ResponseEntity.ok().body(new ApiResponseMessage(ApiResponseMessage.OK, path.toString()));
    } catch (AlreadyExistsException e) {
      state = LogEntryState.FAILURE;
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponseMessage(ApiResponseMessage.ERROR, "File already Exist"));
    } catch (GenericException | IOException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }
}
