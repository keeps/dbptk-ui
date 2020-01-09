package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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

  @GET
  @Path("databases/{databaseUUID}/collection/{collectionUUID}")
  @ApiOperation(value = "", response = CollectionStatus.class)
  CollectionStatus getCollectionStatus(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID);

  @POST
  @Path("databases/{databaseUUID}/collection/{collectionUUID}")
  @ApiOperation(value = "", response = Boolean.class)
  Boolean updateCollectionStatus(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("collectionUUID") String collectionUUID, @ApiParam("collectionStatus") CollectionStatus status);

  @GET
  @Path("/denormalize/{databaseuuid}/{tableuuid}")
  DenormalizeConfiguration getDenormalizeConfigurationFile(@PathParam("databaseuuid") String databaseuuid,
    @PathParam("tableuuid") String tableuuid);

  @POST
  @Path("/denormalize/{databaseuuid}/{tableuuid}")
  Boolean createDenormalizeConfigurationFile(@PathParam("databaseuuid") String databaseuuid,
    @PathParam("tableuuid") String tableuuid, DenormalizeConfiguration configuration);

  @DELETE
  @Path("/denormalize/{databaseuuid}/{tableuuid}")
  Boolean deleteDenormalizeConfigurationFile(@PathParam("databaseuuid") String databaseuuid,
                                             @PathParam("tableuuid") String tableuuid);

  @GET
  @Path("/{databaseuuid}")
  CollectionConfiguration getConfiguration(@PathParam("databaseuuid") String databaseuuid);

  @POST
  @Path("/{databaseuuid}")
  Boolean createConfigurationBundle(@PathParam("databaseuuid") String databaseuuid,
    CollectionConfiguration configuration);

  @POST
  @Path("/{databaseuuid}/{tableuuid}")
  Boolean updateDenormalizeConfiguration(@PathParam("databaseuuid") String databaseuuid,
    @PathParam("tableuuid") String tableuuid, ViewerJobStatus status);
}
