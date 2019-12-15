package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.*;

import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.ProgressData;
import com.databasepreservation.common.client.models.configuration.Configuration;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.ApiOperation;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_CONFIGURATION)
public interface ConfigurationService extends DirectRestService {

  class Util {
    /**
     * @return the singleton instance
     */
    public static ConfigurationService get() {
      return GWT.create(ConfigurationService.class);
    }

    public static <T> ConfigurationService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ConfigurationService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> ConfigurationService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @POST
  @Path("/{databaseuuid}")
  @ApiOperation(value = "retrieves the first 5 rows of the query execution", notes = "", response = ProgressData.class, responseContainer = "database metadata")
  Boolean createConfigurationFile(@PathParam("databaseuuid") String databaseuuid, CollectionConfiguration configuration);

  @GET
  @Path("/{databaseuuid}")
  CollectionConfiguration getConfigurationFile(@PathParam("databaseuuid") String databaseuuid);

  @GET
  @Path("/denormalize/{databaseuuid}/{tableuuid}")
  DenormalizeConfiguration getDenormalizeConfigurationFile(@PathParam("databaseuuid") String databaseuuid, @PathParam("tableuuid") String tableuuid);

  @POST
  @Path("/denormalize/{databaseuuid}/{tableuuid}")
  Boolean createDenormalizeConfigurationFile(@PathParam("databaseuuid") String databaseuuid, @PathParam("tableuuid") String tableeuuid, DenormalizeConfiguration configuration);

  @POST
  @Path("/process/denormalize/{databaseuuid}")
  Boolean denormalize(@PathParam("databaseuuid") String databaseuuid);
}
