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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.wizard.CreateSIARDParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionResponse;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_MIGRATION)
@Tag(name = MigrationService.SWAGGER_ENDPOINT)
public interface MigrationService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 migration";

  class Util {
    /**
     * @return the singleton instance
     */
    public static MigrationService get() {
      return GWT.create(MigrationService.class);
    }

    public static <T> MigrationService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> MigrationService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> MigrationService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @RequestMapping(path = "/siard/modules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the DBPTK developer SIARD migration modules")
  List<Module> getSiardModules(@RequestParam(name = "type") String type,
    @RequestParam(name = "moduleName", required = false) String moduleName);

  @RequestMapping(path = "/dbms/modules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the DBPTK developer DBMS migration modules")
  List<Module> getDBMSModules(@RequestParam(name = "type") String type,
    @RequestParam(name = "moduleName", required = false) String moduleName);

  @RequestMapping(path = "/filter/modules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the DBPTK developer filter modules")
  List<Module> getFilterModules(@RequestParam(name = "moduleName") String moduleName);

  @RequestMapping(path = "/dbms/test/connection", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Tests the connection to the database")
  ConnectionResponse testConnection(
    @Parameter(name = "DBMS connection parameters") @RequestBody final ConnectionParameters connectionParameters);

  @RequestMapping(path = "/dbms/test/query", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the first 5 rows of the query execution")
  List<List<String>> testQuery(@Parameter(name = "connection parameters") ConnectionParameters parameters,
    @RequestParam(name = "query") String query);

  @RequestMapping(path = "/dbms/metadata", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the metadata information associated with the database schema")
  ViewerMetadata getMetadata(
    @Parameter(name = "connection parameters") @RequestBody final ConnectionParameters connectionParameters);

  @RequestMapping(path = "/run", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Performs the migration operation")
  StringResponse run(@RequestParam(name = "databaseUUID") String databaseUUID,
    @Parameter(name = "parameters") @RequestBody CreateSIARDParameters parameters);
}
