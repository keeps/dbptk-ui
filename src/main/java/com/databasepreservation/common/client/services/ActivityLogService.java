/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

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

import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_ACTIVITY_LOG)
@Tag(name = ActivityLogService.SWAGGER_ENDPOINT)
public interface ActivityLogService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 activity log";

  @RequestMapping(path = "/find", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Finds logs", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)), responses = {
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = IndexResult.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiResponseMessage.class)))})
  IndexResult<ActivityLogEntry> find(
    @Parameter(name = "f", description = "Find request to filter/limit the search", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestBody FindRequest findRequest,
    @Parameter(name = ViewerConstants.API_QUERY_PARAM_LOCALE) @RequestParam(name = ViewerConstants.API_QUERY_PARAM_LOCALE) String locale);

  @RequestMapping(path = "/{logUUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a specific log detail")
  ActivityLogWrapper retrieve(
    @Parameter(name = "The unique log identifier") @PathVariable(name = "logUUID") String logUUID);

  class Util {
    /**
     * @return the singleton instance
     */
    public static ActivityLogService get() {
      return GWT.create(ActivityLogService.class);
    }

    public static <T> ActivityLogService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ActivityLogService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> ActivityLogService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }
}
