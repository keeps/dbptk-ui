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

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.databasepreservation.common.api.v1.utils.StringResponse;
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
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_DATABASE)
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
  @RequestMapping(path = "/{databaseUUID}/collection", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a collection for a database")
  StringResponse createCollection(@PathVariable(name = "databaseUUID") String databaseUUID);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the progress data associated with an action done in the database")
  ProgressData getProgressData(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deletes the collection for a specific database")
  Boolean deleteCollection(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID);

  /*******************************************************************************
   * Collection Resource - Config Sub-resource
   *******************************************************************************/
  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the internal collection configuration")
  List<CollectionStatus> getCollectionConfiguration(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/config", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates the internal collection configuration")
  Boolean updateCollectionConfiguration(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID,
    @Parameter(name = "collectionStatus", required = true) @RequestBody CollectionStatus status);

  /*******************************************************************************
   * Collection Resource - Config Sub-resource - Denormalization Sub-resource
   *******************************************************************************/
  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the denormalization configuration file for a certain table within a database")
  DenormalizeConfiguration getDenormalizeConfigurationFile(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @PathVariable(name = "tableUUID") String tableUUID);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates the denormalization configuration file for a certain table within a database")
  Boolean createDenormalizeConfigurationFile(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @PathVariable(name = "tableUUID") String tableUUID,
    @RequestBody DenormalizeConfiguration configuration);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deletes the denormalization configuration file for a certain table within a database")
  Boolean deleteDenormalizeConfigurationFile(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @PathVariable(name = "tableUUID") String tableUUID);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/config/{tableUUID}/run", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Runs a specific denormalization configuration for a certain table within a database")
  void run(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @PathVariable(name = "tableUUID") String tableUUID);

  /*******************************************************************************
   * Collection Resource - Data Sub-resource
   *******************************************************************************/
  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/find", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Find all rows for a specific database")
  IndexResult<ViewerRow> findRows(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @PathVariable(name = "schema") String schema,
    @PathVariable(name = "table") String table,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) @RequestBody FindRequest findRequest,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_LOCALE) @RequestParam(name = ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a specific row within a specific database")
  ViewerRow retrieveRow(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @PathVariable(name = "schema") String schema,
    @PathVariable(name = "table") String table, @PathVariable(name = "rowIndex") String rowIndex);

  /*******************************************************************************
   * Collection Resource - SavedSearch Sub-resource
   *******************************************************************************/
  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/savedSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Saves a search for a specific table within a database")
  StringResponse saveSavedSearch(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID, @RequestParam(name = "tableUUID") String tableUUID,
    @RequestParam(name = "name") String name, @RequestParam(name = "description") String description,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_SEARCH) @RequestBody SearchInfo searchInfo);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/savedSearch/find", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Finds all the saved search for a specific database")
  IndexResult<SavedSearch> findSavedSearches(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) @RequestBody FindRequest findRequest,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_LOCALE) @RequestParam(name = ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a specific saved search for a specific database")
  SavedSearch retrieveSavedSearch(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID,
    @PathVariable(name = "savedSearchUUID") String savedSearchUUID);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}", method = RequestMethod.PUT)
  @Operation(summary = "Edits the content of a search")
  void updateSavedSearch(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID,
    @PathVariable(name = "savedSearchUUID") String savedSearchUUID,
    @Parameter(name = "The saved search name", required = true) @RequestParam(name = "name") String name,
    @Parameter(name = "The saved search description", required = true) @RequestParam(name = "description") String description);

  @RequestMapping(path = "/{databaseUUID}/collection/{collectionUUID}/savedSearch/{savedSearchUUID}", method = RequestMethod.DELETE)
  @Operation(summary = "Deletes a specific saved search for a specific database")
  void deleteSavedSearch(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "collectionUUID") String collectionUUID,
    @PathVariable(name = "savedSearchUUID") String savedSearchUUID);

}
