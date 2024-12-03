/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
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
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_DATABASE)
@Tag(name = DatabaseService.SWAGGER_ENDPOINT)
public interface DatabaseService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 database";

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
  @RequestMapping(path = "/find", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Finds databases")
  IndexResult<ViewerDatabase> find(@RequestBody FindRequest findRequest,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_LOCALE) @RequestParam(name = ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @RequestMapping(path = "/findAll", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Find in all databases")
  IndexResult<ViewerDatabase> findAll(@RequestBody FindRequest findRequest,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_LOCALE) @RequestParam(name = ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @RequestMapping(path = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a database")
  StringResponse create(@Parameter(name = "path") @RequestParam(name = "path") String path,
    @Parameter(name = "version") @RequestParam(defaultValue = "V2_1", name = "version") ViewerConstants.SiardVersion version);

  @RequestMapping(path = "/{databaseUUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves a specific database")
  ViewerDatabase retrieve(@PathVariable(name = "databaseUUID") String databaseUUID);

  @RequestMapping(path = "/{databaseUUID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deletes a specific database")
  Boolean delete(@PathVariable(name = "databaseUUID") String databaseUUID);

  @RequestMapping(path = "/{databaseUUID}/permissions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the internal database configuration")
  Map<String, AuthorizationDetails> getDatabasePermissions(@PathVariable(name = "databaseUUID") String databaseUUID);

  @RequestMapping(path = "/{databaseUUID}/permissions", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates database permissions")
  Map<String, AuthorizationDetails> updateDatabasePermissions(@PathVariable(name = "databaseUUID") String databaseUUID,
                                                              @Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) @RequestBody Map<String, AuthorizationDetails> permissions);

  @RequestMapping(path = "/{databaseUUID}/searchable", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates database permissions")
  boolean updateDatabaseSearchAllAvailability(@PathVariable(name = "databaseUUID") String databaseUUID);

}
