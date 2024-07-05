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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.user.User;
import com.google.gwt.core.client.GWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@RequestMapping(path = ".." + ViewerConstants.ENDPOINT_AUTHENTICATION)
@Tag(name = AuthenticationService.SWAGGER_ENDPOINT)
public interface AuthenticationService extends DirectRestService {
  public static final String SWAGGER_ENDPOINT = "v1 authentication";

  class Util {
    /**
     * @return the singleton instance
     */
    public static AuthenticationService get() {
      return GWT.create(AuthenticationService.class);
    }

    public static <T> AuthenticationService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> AuthenticationService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> AuthenticationService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @RequestMapping(path = "/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Checks if authentication is enabled")
  Boolean isAuthenticationEnabled();

  @RequestMapping(path = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Gets the authenticated user")
  User getAuthenticatedUser();
}
