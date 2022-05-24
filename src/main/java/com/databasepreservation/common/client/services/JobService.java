/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_JOB)
@Tag(name = JobService.SWAGGER_ENDPOINT)
public interface JobService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 job";

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
  @Path("/find")
  @Operation(summary = "Find all jobs")
  IndexResult<ViewerJob> find(@Parameter(name = ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
      @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String locale);
}
