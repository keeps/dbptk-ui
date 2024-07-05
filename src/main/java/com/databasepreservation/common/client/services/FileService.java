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
import org.roda.core.data.common.RodaConstants;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.databasepreservation.common.api.utils.ApiResponseMessage;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_FILE)
@Tag(name = FileService.SWAGGER_ENDPOINT)
public interface FileService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 file";

  class Util {
    /**
     * @return the singleton instance
     */
    public static FileService get() {
      return GWT.create(FileService.class);
    }

    public static <T> FileService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> FileService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> FileService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Lists all the SIARD files in the server")
  List<String> list();

  @RequestMapping(path = "/download/siard", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Downloads a specific SIARD file from the storage location")
  ResponseEntity<Resource> getSIARDFile(
    @Parameter(required = true, name = "The name of the SIARD file to download") @RequestParam(name = ViewerConstants.API_PATH_PARAM_FILENAME) String filename);

  @RequestMapping(method = RequestMethod.DELETE)
  @Operation(summary = "Deletes a SIARD file")
  void deleteSiardFile(
    @Parameter(required = true, name = "Filename to be deleted") @RequestParam(name = ViewerConstants.API_PATH_PARAM_FILENAME) String filename);

  @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Creates a new SIARD file", responses = {
    @ApiResponse(responseCode = "204", description = "File created"),
    @ApiResponse(responseCode = "409", description = "Already exists")})
  ResponseEntity<ApiResponseMessage> createSIARDFile(
    @Parameter(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = MultipartFile.class)), description = "Multipart file") @RequestPart(value = "resource") MultipartFile resource,
    @Parameter(description = "Choose format in which to get the response", schema = @Schema(implementation = RodaConstants.ListMediaTypes.class, defaultValue = RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_JSON)) @RequestParam(name = RodaConstants.API_QUERY_KEY_ACCEPT_FORMAT, required = false) String acceptFormat);
}
