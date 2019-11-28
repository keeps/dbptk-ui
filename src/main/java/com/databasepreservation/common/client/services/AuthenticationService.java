package com.databasepreservation.common.client.services;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.roda.core.data.v2.user.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.function.Consumer;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_AUTHENTICATION)
public interface AuthenticationService extends DirectRestService {
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
  Boolean isAuthenticationEnabled();

  @GET
  @Path("/user")
  @Produces(MediaType.APPLICATION_JSON)
  User getAuthenticatedUser();

  @POST
  @Path("/login")
  @Produces(MediaType.APPLICATION_JSON)
  User login(@QueryParam("username") String username, String password);
}
