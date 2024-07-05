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

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.progress.ValidationProgressData;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_DATABASE)
@Tag(name = SiardService.SWAGGER_ENDPOINT)
public interface SiardService extends DirectRestService {
  String SWAGGER_ENDPOINT = "v1 SIARD";

  class Util {
    /**
     * @return the singleton instance
     */
    public static SiardService get() {
      return GWT.create(SiardService.class);
    }

    public static <T> SiardService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> SiardService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> SiardService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deletes a specific SIARD file in the storage location")
  void deleteSIARDFile(
    @Parameter(name = "The database unique identifier", required = true) @PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID);

  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the SIARD within a specific database")
  ViewerDatabase getSiard(
    @Parameter(name = "The database unique identifier", required = true) @PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID);

  /*******************************************************************************
   * Validation Sub-resource
   *******************************************************************************/
  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}/validation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Validates the SIARD file against the specification")
  Boolean validateSiard(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID,
    @RequestParam(name = "validationReportPath", required = false) String validationReportPath,
    @RequestParam(name = "allowedTypePath", required = false) String allowedTypePath,
    @RequestParam(name = "skipAdditionalChecks", required = false) boolean skipAdditionalChecks);

  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}/validation/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the validation progress")
  ValidationProgressData getValidationProgressData(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID);

  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}/validation", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deletes the report generated for the validation of a SIARD file in the storage location")
  void deleteValidationReport(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID, @RequestParam(name = "path") String path);

  /*******************************************************************************
   * Metadata Sub-resource
   *******************************************************************************/
  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}/metadata", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Updates the SIARD metadata information")
  ViewerMetadata updateMetadataInformation(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID, @RequestParam(name = "path") String path,
    @RequestBody SIARDUpdateParameters parameters, @RequestParam(name = "updateOnModel") boolean updateOnModel);

  @RequestMapping(path = "/{databaseUUID}/siard/{siardUUID}/metadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the SIARD metadata information")
  ViewerMetadata getMetadataInformation(@PathVariable(name = "databaseUUID") String databaseUUID,
    @PathVariable(name = "siardUUID") String siardUUID);
}
