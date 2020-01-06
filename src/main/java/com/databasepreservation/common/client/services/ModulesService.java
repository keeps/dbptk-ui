package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.ConnectionResponse;
import com.databasepreservation.common.client.models.DBPTKModule;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Path(".." + ViewerConstants.ENDPOINT_MODULES)
@Api(value = ModulesService.SWAGGER_ENDPOINT)
public interface ModulesService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 modules";

  class Util {
    /**
     * @return the singleton instance
     */
    public static ModulesService get() {
      return GWT.create(ModulesService.class);
    }

    public static <T> ModulesService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ModulesService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> ModulesService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @GET
  @Path("/live/import")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Retrieves the import DBPTK developer modules", notes = "", response = DBPTKModule.class)
  DBPTKModule getImportDBPTKModules() throws RESTException;

  @GET
  @Path("/live/export")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Retrieves the export DBPTK developer modules", notes = "", response = DBPTKModule.class)
  DBPTKModule getExportDBPTKModules() throws RESTException;

  @GET
  @Path("/siard/export/{moduleName}")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Gets the specific export DBPTK developer module", notes = "", response = DBPTKModule.class)
  DBPTKModule getSIARDExportModule(@PathParam("moduleName") String moduleName);

  @GET
  @Path("/siard/export/all")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Find all the export DBPTK developer module", notes = "", response = DBPTKModule.class)
  DBPTKModule getSIARDExportModules();

  @POST
  @Path("/test/connection")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "Tests the connection to the database", notes = "", response = ConnectionResponse.class)
  ConnectionResponse testDBConnection(@ApiParam(value = "DBMS connection parameters") final ConnectionParameters connectionParameters);
}
