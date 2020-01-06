package com.databasepreservation.common.client.services;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.DenormalizeProgressData;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import javax.batch.runtime.JobInstance;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_JOB)
public interface JobService extends DirectRestService {
  class Util {
    /**
     * @return the singleton instance
     */
    public static JobService get() {
      return GWT.create(JobService.class);
    }

    public static <T> JobService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> JobService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> JobService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @POST
  @Path("/{databaseuuid}")
  Boolean denormalizeJob(@PathParam("databaseuuid") String databaseuuid);

  @POST
  @Path("/stop/{databaseuuid}/{tableuuid}")
  Boolean stopDenormalizeJob(@PathParam("databaseuuid") String databaseuuid, @PathParam("tableuuid") String tableuuid);

  @POST
  @Path("/start/{databaseuuid}/{tableuuid}")
  Boolean startDenormalizeJob(@PathParam("databaseuuid") String databaseuuid, @PathParam("tableuuid") String tableuuid);

  @GET
  @Path("/progress/{databaseuuid}")
  List<DenormalizeProgressData> progress(@PathParam("databaseuuid") String databaseuuid);

  @POST
  @Path("/jobs")
  IndexResult<ViewerJob> findJobs(FindRequest filter);

}
