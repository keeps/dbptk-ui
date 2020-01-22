package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.progress.ValidationProgressData;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
@Api(value = SiardService.SWAGGER_ENDPOINT)
public interface SiardService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 SIARD";

  class Util {
    /**
     * @return the singleton instance
     */
    public static SiardService get() {
      return GWT.create(SiardService.class);
    }

    public static <T> SiardService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> SiardService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> SiardService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @DELETE
  @Path("/{databaseUUID}/siard/{siardUUID}")
  @ApiOperation(value = "Deletes a specific SIARD file in the storage location", notes = "")
  void deleteSIARDFile(
    @ApiParam(value = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}")
  @ApiOperation(value = "Retrieves the SIARD within a specific database", notes = "", response = ViewerDatabase.class)
  ViewerDatabase getSiard(
    @ApiParam(value = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  /*******************************************************************************
   * Validation Sub-resource
   *******************************************************************************/
  @POST
  @Path("/{databaseUUID}/siard/{siardUUID}/validation")
  @ApiOperation(value = "Validates the SIARD file against the specification", notes = "", response = Boolean.class)
  Boolean validateSiard(@PathParam("databaseUUID") String databaseUUID, @PathParam("siardUUID") String siardUUID,
    @QueryParam("validationReportPath") String validationReportPath,
    @QueryParam("allowedTypePath") String allowedTypePath,
    @QueryParam("skipAdditionalChecks") boolean skipAdditionalChecks);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}/validation/status")
  @ApiOperation(value = "Retrieves the validation progress", notes = "", response = ValidationProgressData.class)
  ValidationProgressData getValidationProgressData(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("/{databaseUUID}/siard/{siardUUID}/validation")
  @ApiOperation(value = "Deletes the report generated for the validation of a SIARD file in the storage location", notes = "")
  void deleteValidationReport(@PathParam("databaseUUID") String databaseUUID, @QueryParam("path") String path);

  /*******************************************************************************
   * Metadata Sub-resource
   *******************************************************************************/
  @PUT
  @Path("/{databaseUUID}/siard/{siardUUID}/metadata")
  @ApiOperation(value = "Updates the SIARD metadata information", notes = "", response = ViewerMetadata.class)
  ViewerMetadata updateMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID, @QueryParam("path") String path, SIARDUpdateParameters parameters);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}/metadata")
  @ApiOperation(value = "Gets the SIARD metadata information", notes = "", response = ViewerMetadata.class)
  ViewerMetadata getMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);
}
