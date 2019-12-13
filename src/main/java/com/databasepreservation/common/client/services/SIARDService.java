package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.DBPTKModule;
import com.databasepreservation.common.client.models.ValidationProgressData;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.models.parameters.CreateSIARDParameters;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_SIARD)
@Api(value = SIARDService.SWAGGER_ENDPOINT)
public interface SIARDService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 siard";

  class Util {
    /**
     * @return the singleton instance
     */
    public static SIARDService get() {
      return GWT.create(SIARDService.class);
    }

    public static <T> SIARDService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> SIARDService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> SIARDService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @POST
  @Path("upload/{databaseUUID}")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  String uploadSIARD(@PathParam("databaseUUID") String databaseUUID, @QueryParam("siardPath") String path)
    throws RESTException;

  @POST
  @Path("uploadMetadata")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  String uploadMetadataSIARDServer(@QueryParam("siardPath") String path) throws RESTException;

  @POST
  @Path("uploadMetadata/{databaseUUID}")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  String uploadMetadataSIARD(@PathParam("databaseUUID") String databaseUUID, @QueryParam("siardPath") String path)
    throws RESTException;

  @GET
  @Path("find")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  String findSIARDFile(@QueryParam("siardPath") String path) throws RESTException;

  @POST
  @Path("create/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  Boolean createSIARD(@PathParam("databaseUUID") String databaseUUID, CreateSIARDParameters parameters)
    throws RESTException;

  @POST
  @Path("migrateToDbms/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  Boolean migrateToDBMS(@PathParam("databaseUUID") String databaseUUID, @QueryParam("siardVersion") String siardVersion,
    @QueryParam("siardPath") String siardPath, ConnectionParameters parameters) throws RESTException;

  @POST
  @Path("migrateToSiard/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  Boolean migrateToSIARD(@PathParam("databaseUUID") String databaseUUID,
    @QueryParam("siardVersion") String siardVersion, @QueryParam("siardPath") String siardPath,
    CreateSIARDParameters parameters) throws RESTException;

  @POST
  @Path("updateMetadataInformation/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  ViewerMetadata updateMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @QueryParam("path") String path, SIARDUpdateParameters parameters) throws RESTException;

  @POST
  @Path("validate/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  Boolean validateSIARD(@PathParam("databaseUUID") String databaseUUID, @QueryParam("SIARDPath") String SIARDPath,
    @QueryParam("validationReportPath") String validationReportPath,
    @QueryParam("allowedTypePath") String allowedTypePath,
    @QueryParam("skipAdditionalChecks") boolean skipAdditionalChecks) throws RESTException;

  @GET
  @Path("validateProgress/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  ValidationProgressData getValidationProgressData(@PathParam("databaseUUID") String databaseUUID);

  @POST
  @Path("validateProgressClear/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  void clearValidationProgressData(@PathParam("databaseUUID") String databaseUUID);

  @POST
  @Path("validateUpdate/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  void updateStatusValidate(@PathParam("databaseUUID") String databaseUUID, @QueryParam("status") ViewerDatabaseValidationStatus status);

  @POST
  @Path("deleteSIARDFile/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  void deleteSIARDFile(@PathParam("databaseUUID") String databaseUUID, @QueryParam("path") String path);

  @POST
  @Path("deleteSIARDValidatorReportFile/{databaseUUID}")
  @ApiOperation(value = "retrieve DBPTK import modules", notes = "Export query results as CSV.", response = DBPTKModule.class, responseContainer = "CSVExport")
  void deleteSIARDValidatorReportFile(@PathParam("databaseUUID") String databaseUUID, @QueryParam("path") String path);

}