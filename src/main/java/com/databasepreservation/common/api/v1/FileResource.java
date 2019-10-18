package com.databasepreservation.common.api.v1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.io.Files;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.JSONP;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.ExtraMediaType;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.controller.Browser;
import com.databasepreservation.common.shared.ViewerConstants;

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
  @ApiOperation(value = "Create file", notes = "Create a new SIARD file", response = Void.class)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "OK", response = Void.class),
    @ApiResponse(code = 409, message = "Already exists", response = ApiResponseMessage.class)})

  public Response createSIARDFile(@FormDataParam("upl") InputStream inputStream,
    @FormDataParam("upl") FormDataContentDisposition fileDetail,
    @ApiParam(value = "Choose format in which to get the response", allowableValues = RodaConstants.API_POST_PUT_MEDIA_TYPES) @QueryParam(RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT) String acceptFormat,
    @ApiParam(value = "JSONP callback name", required = false, allowMultiple = false, defaultValue = RodaConstants.API_QUERY_DEFAULT_JSONP_CALLBACK) @QueryParam(RodaConstants.API_QUERY_KEY_JSONP_CALLBACK) String jsonpCallbackName)
    throws RODAException {
    String mediaType = ApiUtils.getMediaType(acceptFormat, request);
    String fileExtension = Files.getFileExtension(fileDetail.getFileName());

    if(!fileExtension.equals(ViewerConstants.SIARD)){
      return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Must be a SIARD file")).build();
    }

    java.nio.file.Path path = Paths.get(ViewerConfiguration.getInstance().getSIARDFilesPath().toString(),
      fileDetail.getFileName());

    // delegate action to controller
    try {
      Browser.createFile(inputStream, fileDetail.getFileName(), path);
    } catch (AlreadyExistsException e) {
      return Response.status(Response.Status.CONFLICT).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "File already Exist")).build();
    }

    return Response.ok(new ApiResponseMessage(ApiResponseMessage.OK, path.toString()), mediaType).build();
  }
}
