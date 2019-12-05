package com.databasepreservation.common.client.services;

import com.databasepreservation.common.api.v1.ExportsResource;
import com.databasepreservation.common.client.models.ConnectionResponse;
import com.databasepreservation.common.client.models.DBPTKModule;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.google.gwt.core.client.GWT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Consumer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Path(".." + ViewerConstants.ENDPOINT_MODULES)
@Api(value = ExportsResource.SWAGGER_ENDPOINT)
public interface ModulesService extends DirectRestService {

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
  @ApiOperation(value = "retrieves DBPTK import modules", notes = "", response = DBPTKModule.class, responseContainer = "DBPTK Modules")
  DBPTKModule getImportDBPTKModules() throws RESTException;

  @GET
  @Path("/live/export")
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "retrieves DBPTK export modules", notes = "", response = DBPTKModule.class, responseContainer = "DBPTK Modules")
  DBPTKModule getExportDBPTKModules() throws RESTException;

  @GET
  @Path("/siard/export/{modulename}")
  @Produces({MediaType.APPLICATION_JSON})
  DBPTKModule getSIARDExportModule(@PathParam("modulename") String moduleName);

  @GET
  @Path("/siard/export/all")
  @Produces({MediaType.APPLICATION_JSON})
  DBPTKModule getSIARDExportModules();

  @POST
  @Path("/test/connection")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "tests the connection to the database", notes = "Export query results as CSV.", response = ConnectionResponse.class, responseContainer = "CSVExport")
  ConnectionResponse testDBConnection(@ApiParam(value = "Find parameters") final ConnectionParameters connectionParameters);
}
