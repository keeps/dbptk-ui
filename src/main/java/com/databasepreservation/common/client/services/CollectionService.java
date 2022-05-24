/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_DATABASE)
@Tag(name = CollectionService.SWAGGER_ENDPOINT)
public interface CollectionService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 collection";

  class Util {
    /**
     * @return the singleton instance
     */
    public static CollectionService get() {
      return GWT.create(CollectionService.class);
    }

    public static <T> CollectionService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> CollectionService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> CollectionService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  /*******************************************************************************
   * Collection Resource
   *******************************************************************************/
  @POST
  @Path("{databaseUUID}/collection")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Creates a collection for a database")
  String createCollection(@PathParam("databaseUUID") String databaseUUID);

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/status")
  @Operation(summary = "Retrieves the progress data associated with an action done in the database")
  ProgressData getProgressData(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID);

  @DELETE
  @Path("{databaseUUID}/collection/{collectionUUID}")
  @Operation(summary = "Deletes the collection for a specific database")
  Boolean deleteCollection(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID);

  /*******************************************************************************
   * Collection Resource - Config Sub-resource
   *******************************************************************************/
  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/config")
  @Operation(summary = "Gets the internal collection configuration")
  List<CollectionStatus> getCollectionConfiguration(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID);

  @PUT
  @Path("{databaseUUID}/collection/{collectionUUID}/config")
  @Operation(summary = "Updates the internal collection configuration")
  Boolean updateCollectionConfiguration(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID,
      @Parameter(name = "collectionStatus", required = true) CollectionStatus status);

  /*******************************************************************************
   * Collection Resource - Config Sub-resource - Denormalization Sub-resource
   *******************************************************************************/
  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}")
  @Operation(summary = "Gets the denormalization configuration file for a certain table within a database")
  DenormalizeConfiguration getDenormalizeConfigurationFile(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("tableUUID") String tableUUID);

  @POST
  @Path("{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}")
  @Operation(summary = "Creates the denormalization configuration file for a certain table within a database")
  Boolean createDenormalizeConfigurationFile(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("tableUUID") String tableUUID,
      DenormalizeConfiguration configuration);

  @DELETE
  @Path("{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}")
  @Operation(summary = "Deletes the denormalization configuration file for a certain table within a database")
  Boolean deleteDenormalizeConfigurationFile(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("tableUUID") String tableUUID);

  @GET
  @Path("{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}/run")
  @Operation(summary = "Runs a specific denormalization configuration for a certain table within a database")
  void run(@PathParam("databaseUUID") String databaseUUID, @PathParam("collectionUUID") String collectionUUID,
      @PathParam("tableUUID") String tableUUID);

  /*******************************************************************************
   * Collection Resource - Data Sub-resource
   *******************************************************************************/
  @POST
  @Path("{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/find")
  @Operation(summary = "Find all rows for a specific database")
  IndexResult<ViewerRow> findRows(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("schema") String schema,
      @PathParam("table") String table, @Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
      @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}")
  @Operation(summary = "Retrieves a specific row within a specific database")
  ViewerRow retrieveRow(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("schema") String schema,
      @PathParam("table") String table, @PathParam("rowIndex") String rowIndex);

  /*******************************************************************************
   * Collection Resource - SavedSearch Sub-resource
   *******************************************************************************/
  @POST
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Saves a search for a specific table within a database")
  String saveSavedSearch(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @QueryParam("tableUUID") String tableUUID,
      @QueryParam("name") String name, @QueryParam("description") String description,
      @Parameter(name = ViewerConstants.API_QUERY_PARAM_SEARCH) SearchInfo searchInfo);

  @POST
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/find")
  @Operation(summary = "Finds all the saved search for a specific database")
  IndexResult<SavedSearch> findSavedSearches(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID,
      @Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
      @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @GET
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}")
  @Operation(summary = "Retrieves a specific saved search for a specific database")
  SavedSearch retrieveSavedSearch(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("savedSearchUUID") String savedSearchUUID);

  @PUT
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}")
  @Operation(summary = "Edits the content of a search")
  void updateSavedSearch(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("savedSearchUUID") String savedSearchUUID,
      @Parameter(name = "The saved search name", required = true) @QueryParam("name") String name,
      @Parameter(name = "The saved search description", required = true) @QueryParam("description") String description);

  @DELETE
  @Operation(summary = "Deletes a specific saved search for a specific database")
  @Path("/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}")
  void deleteSavedSearch(@PathParam("databaseUUID") String databaseUUID,
      @PathParam("collectionUUID") String collectionUUID, @PathParam("savedSearchUUID") String savedSearchUUID);

}
