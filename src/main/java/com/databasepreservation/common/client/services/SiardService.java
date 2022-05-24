/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
@Tag(name = SiardService.SWAGGER_ENDPOINT)
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
  @Operation(summary = "Deletes a specific SIARD file in the storage location")
  void deleteSIARDFile(
    @Parameter(name = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}")
  @Operation(summary = "Retrieves the SIARD within a specific database")
  ViewerDatabase getSiard(
    @Parameter(name = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  /*******************************************************************************
   * Validation Sub-resource
   *******************************************************************************/
  @POST
  @Path("/{databaseUUID}/siard/{siardUUID}/validation")
  @Operation(summary = "Validates the SIARD file against the specification")
  Boolean validateSiard(@PathParam("databaseUUID") String databaseUUID, @PathParam("siardUUID") String siardUUID,
    @QueryParam("validationReportPath") String validationReportPath,
    @QueryParam("allowedTypePath") String allowedTypePath,
    @QueryParam("skipAdditionalChecks") boolean skipAdditionalChecks);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}/validation/status")
  @Operation(summary = "Retrieves the validation progress")
  ValidationProgressData getValidationProgressData(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  @DELETE
  @Path("/{databaseUUID}/siard/{siardUUID}/validation")
  @Operation(summary = "Deletes the report generated for the validation of a SIARD file in the storage location")
  void deleteValidationReport(@PathParam("databaseUUID") String databaseUUID, @PathParam("siardUUID") String siardUUID,
    @QueryParam("path") String path);

  /*******************************************************************************
   * Metadata Sub-resource
   *******************************************************************************/
  @PUT
  @Path("/{databaseUUID}/siard/{siardUUID}/metadata")
  @Operation(summary = "Updates the SIARD metadata information")
  ViewerMetadata updateMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID, @QueryParam("path") String path, SIARDUpdateParameters parameters,
    @QueryParam("updateOnModel") boolean updateOnModel);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}/metadata")
  @Operation(summary = "Gets the SIARD metadata information")
  ViewerMetadata getMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);
}
