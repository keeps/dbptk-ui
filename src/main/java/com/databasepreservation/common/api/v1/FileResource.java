/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.controller.Browser;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.google.common.io.Files;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(FileResource.ENDPOINT)
@Api(value = FileResource.SWAGGER_ENDPOINT)
public class FileResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileResource.class);
  public static final String ENDPOINT = "/" + ViewerConstants.API_SERVLET + ViewerConstants.API_V1_FILE_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 files";

  @Context
  private HttpServletRequest request;

  @GET
  @ApiOperation(value = "Lists all the SIARD files in the server", notes = "")
  public List<String> list() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    final java.nio.file.Path path = ViewerConfiguration.getInstance().getSIARDFilesPath();
    try {
      return java.nio.file.Files.walk(path).filter(java.nio.file.Files::isRegularFile).sorted(Comparator.naturalOrder())
        .map(java.nio.file.Path::getFileName).map(java.nio.file.Path::toString).collect(Collectors.toList());
    } catch (IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  @GET
  @Path("/download/siard")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads a specific SIARD file from the storage location", notes = "")
  public Response getSIARDFile(
    @ApiParam(required = true, value = "The name of the SIARD file to download") @QueryParam(ViewerConstants.API_PATH_PARAM_FILENAME) String filename) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      java.nio.file.Path siardFilesPath = ViewerConfiguration.getInstance().getSIARDFilesPath();
      java.nio.file.Path basePath = Paths.get(ViewerConfiguration.getInstance().getViewerConfigurationAsString("/",
        ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH));
      java.nio.file.Path siardPath = siardFilesPath.resolve(filename);
      if (java.nio.file.Files.exists(siardPath) && !java.nio.file.Files.isDirectory(siardPath)
        && (ViewerConfiguration.checkPathIsWithin(siardPath, siardFilesPath)
          || ViewerConfiguration.checkPathIsWithin(siardPath, basePath))) {
        Response.ResponseBuilder responseBuilder = Response.ok(siardPath.toFile());
        responseBuilder.header("Content-Disposition", "attachment; filename=\"" + siardPath.toFile().getName() + "\"");
        return responseBuilder.build();
      } else {
        throw new NotFoundException("SIARD file not found");
      }
    } catch (NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  @DELETE
  @ApiOperation(value = "Deletes a SIARD file", notes = "")
  public void deleteSiardFile(
    @ApiParam(value = "Filename to be deleted", required = true) @QueryParam(value = "filename") String filename) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      java.nio.file.Files.walk(ViewerConfiguration.getInstance().getSIARDFilesPath()).map(java.nio.file.Path::toFile)
        .filter(p -> p.getName().equals(filename)).forEach(File::delete);
      LOGGER.info("SIARD file removed from system ({})", filename);
    } catch (IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(new NotFoundException("Could not delete SIARD file: " + filename + " from the system"));
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM, filename);
    }
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @JSONP(callback = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK, queryParam = RodaConstants.API_QUERY_KEY_JSONP_CALLBACK)
  @ApiOperation(value = "Creates a new SIARD file", notes = "")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK"),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createSIARDFile(@FormDataParam("upl") InputStream inputStream,
    @FormDataParam("upl") FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name", required = false, allowMultiple = false, defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    String fileExtension = Files.getFileExtension(fileDetail.getFileName());

    if (!fileExtension.equals(ViewerConstants.SIARD)) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Must be a SIARD file")).build();
    }

    java.nio.file.Path path = Paths.get(ViewerConfiguration.getInstance().getSIARDFilesPath().toString(),
      fileDetail.getFileName());

    // delegate action to controller
    try {
      Browser.createFile(inputStream, fileDetail.getFileName(), path);
      return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, path.toString()), mediaType).build();
    } catch (AlreadyExistsException e) {
      state = LogEntryState.FAILURE;
      return Response.status(Response.Status.CONFLICT)
        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "File already Exist")).build();
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM,
        fileDetail.getFileName());
    }
  }
}