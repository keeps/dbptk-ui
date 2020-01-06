package com.databasepreservation.common.client.services;

import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.google.gwt.core.client.GWT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_AUTHENTICATION)
@Api(value = AuthenticationService.SWAGGER_ENDPOINT)
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

  @GET
  @Path("/isEnable")
  @ApiOperation(value = "Checks if authentication is enabled", response = Boolean.class)
  Boolean isAuthenticationEnabled();

  @GET
  @Path("/user")
  @ApiOperation(value = "Gets the authenticated user", response = User.class)
  @Produces(MediaType.APPLICATION_JSON)
  User getAuthenticatedUser();

  @GET
  @Path("/isAdmin")
  @ApiOperation(value = "Checks if the user is of administrator type", response = Boolean.class)
  Boolean userIsAdmin();

  @POST
  @Path("/login")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Performs the login operation", response = Boolean.class)
  User login(@QueryParam("username") String username, String password);
}
