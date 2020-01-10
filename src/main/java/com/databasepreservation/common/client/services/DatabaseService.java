package com.databasepreservation.common.client.services;

import java.util.List;
import java.util.function.Consumer;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.index.FindNestedRequest;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.models.progress.ValidationProgressData;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
@Api(value = DatabaseService.SWAGGER_ENDPOINT)
public interface DatabaseService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 database";

  class Util {
    /**
     * @return the singleton instance
     */
    public static DatabaseService get() {
      return GWT.create(DatabaseService.class);
    }

    public static <T> DatabaseService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> DatabaseService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> DatabaseService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  /*******************************************************************************
   * Database Resource
   *******************************************************************************/
  @POST
  @Path("/")
  @ApiOperation(value = "Finds databases", notes = "", response = ViewerDatabase.class, responseContainer = "IndexResult")
  IndexResult<ViewerDatabase> findDatabases(@ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest filter,
    @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @GET
  @Path("/create")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Creates a database", notes = "", response = String.class)
  String createDatabase(@QueryParam("path") String path);

  @GET
  @Path("/{databaseUUID}")
  @ApiOperation(value = "Retrieves a specific database", notes = "", response = ViewerDatabase.class)
  ViewerDatabase retrieve(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("/{databaseUUID}")
  @ApiOperation(value = "Deletes a specific database", notes = "", response = Boolean.class)
  Boolean deleteDatabase(@PathParam("databaseUUID") String databaseUUID);

  /*******************************************************************************
   * SIARD Resource
   *******************************************************************************/
  @DELETE
  @Path("{databaseUUID}/siard/{siardUUID}")
  @ApiOperation(value = "Deletes a specific SIARD file in the storage location", notes = "")
  void deleteSIARDFile(@PathParam("databaseUUID") String databaseUUID, @PathParam("siardUUID") String siardUUID);

  @GET
  @Path("/{databaseUUID}/siard/{siardUUID}")
  @ApiOperation(value = "Retrieves the SIARD within a specific database", notes = "", response = ViewerDatabase.class)
  ViewerDatabase getSiard(
    @ApiParam(value = "The database unique identifier", required = true) @PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  /*******************************************************************************
   * SIARD Resource - Validation Sub-resource
   *******************************************************************************/
  @POST
  @Path("{databaseUUID}/siard/{siardUUID}/validation")
  @ApiOperation(value = "Validates the SIARD file against the specification", notes = "", response = Boolean.class)
  Boolean validateSiard(@PathParam("databaseUUID") String databaseUUID, @PathParam("databaseUUID") String siardUUID,
    @QueryParam("validationReportPath") String validationReportPath,
    @QueryParam("allowedTypePath") String allowedTypePath,
    @QueryParam("skipAdditionalChecks") boolean skipAdditionalChecks);

  @GET
  @Path("{databaseUUID}/siard/{siardUUID}/validation/status")
  @ApiOperation(value = "Retrieves the validation progress", notes = "", response = ValidationProgressData.class)
  ValidationProgressData getValidationProgressData(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("{databaseUUID}/siard/{siardUUID}/validation")
  @ApiOperation(value = "Deletes the report generated for the validation of a SIARD file in the storage location", notes = "")
  void deleteValidationReport(@PathParam("databaseUUID") String databaseUUID, @QueryParam("path") String path);

  /*******************************************************************************
   * SIARD Resource - Metadata Sub-resource
   *******************************************************************************/
  @PUT
  @Path("{databaseUUID}/siard/{siardUUID}/metadata")
  @ApiOperation(value = "Updates the SIARD metadata information", notes = "", response = ViewerMetadata.class)
  ViewerMetadata updateMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("databaseUUID") String siardUUID, @QueryParam("path") String path, SIARDUpdateParameters parameters);

  @GET
  @Path("{databaseUUID}/siard/{siardUUID}/metadata")
  @ApiOperation(value = "Gets the SIARD metadata information", notes = "", response = ViewerMetadata.class)
  ViewerMetadata getMetadataInformation(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("siardUUID") String siardUUID);

  /*******************************************************************************
   * Collection Resource
   *******************************************************************************/
  @POST
  @Path("{databaseUUID}/collection")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Creates a collection for a database", notes = "", response = String.class)
  String createCollection(@PathParam("databaseUUID") String databaseUUID);

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/status")
  @ApiOperation(value = "Retrieves the progress data associated with an action done in the database", notes = "", response = ProgressData.class)
  ProgressData getProgressData(@PathParam("databaseUUID") String databaseUUID);

  @DELETE
  @Path("{databaseUUID}/collection/{collectionUUID}")
  @ApiOperation(value = "Deletes the collection for a specific database", notes = "", response = Boolean.class)
  Boolean deleteCollection(@PathParam("databaseUUID") String databaseUUID);

  /*******************************************************************************
   * Collection Resource - Table Sub-resource
   *******************************************************************************/
  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/tables")
  @ApiOperation(value = "Gets the internal collection configuration", response = CollectionStatus.class)
  List<CollectionStatus> getCollectionConfiguration(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("databaseUUID") String collectionUUID);

  @PUT
  @Path("{databaseUUID}/collection/{collectionUUID}/tables")
  @ApiOperation(value = "Updates the internal collection configuration", response = Boolean.class)
  Boolean updateCollectionConfiguration(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID,
    @ApiParam(value = "collectionStatus", required = true) CollectionStatus status);

  /*******************************************************************************
   * Collection Resource - Table Sub-resource - Denormalization Sub-resource
   *******************************************************************************/
  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/tables/{tableUUID}")
  @ApiOperation(value = "Gets the denormalization configuration file for a certain table within a database", response = DenormalizeConfiguration.class)
  DenormalizeConfiguration getDenormalizeConfigurationFile(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID, @PathParam("tableUUID") String tableUUID);

  @POST
  @Path("{databaseUUID}/collection/{collectionUUID}/tables/{tableUUID}")
  @ApiOperation(value = "Creates the denormalization configuration file for a certain table within a database", response = Boolean.class)
  Boolean createDenormalizeConfigurationFile(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID, @PathParam("tableUUID") String tableUUID,
    DenormalizeConfiguration configuration);

  @DELETE
  @Path("{databaseUUID}/collection/{collectionUUID}/tables/{tableUUID}")
  @ApiOperation(value = "Deletes the denormalization configuration file for a certain table within a database", response = Boolean.class)
  Boolean deleteDenormalizeConfigurationFile(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID, @PathParam("tableUUID") String tableUUID);

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/tables/{tableUUID}/run")
  @ApiOperation(value = "Runs a specific denormalization configuration for a certain table within a database", response = DenormalizeConfiguration.class)
  Boolean run(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID,
    @PathParam("tableUUID") String tableUUID);

  /*******************************************************************************
   * Collection Resource - Rows Sub-resource
   *******************************************************************************/
  @POST
  @Path("{databaseUUID}/collection/{collectionUUID}/rows")
  @ApiOperation(value = "Find all rows for a specific database", notes = "", response = ViewerRow.class, responseContainer = "IndexResult")
  IndexResult<ViewerRow> findRows(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID,
    @ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
    @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/rows/{rowUUID}")
  @ApiOperation(value = "Retrieves a specific row within a specific database", notes = "", response = ViewerRow.class)
  ViewerRow retrieveRow(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID, @PathParam("rowUUID") String rowUUID);

  /*******************************************************************************
   * Collection Resource - SavedSearch Sub-resource
   *******************************************************************************/
  @POST
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/")
  @ApiOperation(value = "Saves a search for a specific table within a database", notes = "", response = String.class)
  String saveSavedSearch(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID,
                         @QueryParam("tableUUID") String tableUUID, @QueryParam("name") String name,
                         @QueryParam("description") String description,
                         @ApiParam(ViewerConstants.API_QUERY_PARAM_SEARCH) SearchInfo searchInfo);

  @POST
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/find")
  @ApiOperation(value = "Finds all the saved search for a specific database", notes = "", response = SavedSearch.class, responseContainer = "IndexResult")
  IndexResult<SavedSearch> findSavedSearches(@PathParam("databaseUUID") String databaseUUID,
                                             @PathParam("collectionUUID") String collectionUUID,
                                             @ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
                                             @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}")
  @ApiOperation(value = "Retrieves a specific saved search for a specific database", notes = "", response = SavedSearch.class)
  SavedSearch retrieveSavedSearch(@PathParam("databaseUUID") String databaseUUID,
                                  @PathParam("collectionUUID") String collectionUUID, @PathParam("savedSearchUUID") String savedSearchUUID);

  @PUT
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}")
  @ApiOperation(value = "Edits the content of a search", notes = "")
  void updateSavedSearch(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID,
                         @PathParam("savedSearchUUID") String savedSearchUUID,
                         @ApiParam(value = "The saved search name", required = true) @QueryParam("name") String name,
                         @ApiParam(value = "The saved search description", required = true) @QueryParam("description") String description);

  @DELETE
  @ApiOperation(value = "Deletes a specific saved search for a specific database", notes = "")
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}")
  void deleteSavedSearch(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID,
                         @PathParam("savedSearchUUID") String savedSearchUUID);
}
