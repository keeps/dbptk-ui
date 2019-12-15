package com.databasepreservation.common.api.v1;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
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
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.Browser;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;
import com.google.common.io.Files;

import io.swagger.annotations.*;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(FileResource.ENDPOINT)
@Api(value = FileResource.SWAGGER_ENDPOINT)
public class FileResource {
  public static final String ENDPOINT = "/" + ViewerConstants.API_SERVLET + ViewerConstants.API_V1_FILE_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 files";

  @Context
  private HttpServletRequest request;

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
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    String fileExtension = Files.getFileExtension(fileDetail.getFileName());

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

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

  @GET
  @Path(ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_SIARD + ViewerConstants.API_SEP + "{"
    + ViewerConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads a specific SIARD file from the storage location", notes = "")
  public Response getSIARDFile(@PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    try {
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      File file = new File(database.getPath());
      if (!file.exists()) {
        throw new NotFoundException("SIARD file not found");
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

  @GET
  @Path(ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_VALIDATION_REPORT + ViewerConstants.API_SEP + "{"
    + ViewerConstants.API_PATH_PARAM_DATABASE_UUID + "}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads a specific SIARD validation report file from the storage location", notes = "")
  public Response getValidationReportFile(
    @PathParam(ViewerConstants.API_PATH_PARAM_DATABASE_UUID) String databaseUUID) {
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
}