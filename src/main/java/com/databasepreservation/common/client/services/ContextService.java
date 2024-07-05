/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_CONTEXT)
@Tag(name = ContextService.SWAGGER_ENDPOINT)
public interface ContextService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 context";

  class Util {
    /**
     * @return the singleton instance
     */
    public static ContextService get() {
      return GWT.create(ContextService.class);
    }

    public static <T> ContextService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> ContextService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }
  }

  @RequestMapping(path = "/environment", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the environment", hidden = true)
  StringResponse getEnvironment();

  @RequestMapping(path = "/clientMachineHost", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the client machine host", hidden = true)
  StringResponse getClientMachine();

  @RequestMapping(path = "/shared/properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieves the shared properties", hidden = true)
  Map<String, List<String>> getSharedProperties(
    @RequestParam(name = ViewerConstants.API_QUERY_PARAM_LOCALE, defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/authorizations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the authorizations group list")
  Set<AuthorizationGroup> getAuthorizationGroupsList();
}
