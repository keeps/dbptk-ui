package com.databasepreservation.common.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.services.AuthenticationService;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.controller.UserLoginController;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_AUTHENTICATION)
public class AuthenticationResource implements AuthenticationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

  @Context
  private HttpServletRequest request;

  @Override
  public Boolean isAuthenticationEnabled() {
    return ViewerConfiguration.getInstance().getIsAuthenticationEnabled();
  }

  @Override
  public User getAuthenticatedUser() {
    User user = UserUtility.getUser(request);
    LOGGER.debug("Serving user {}", user);
    return user;
  }

  @Override
  public User login(String username, String password) throws RESTException {
    try {
      User user = UserLoginController.login(username, password, request);
      LOGGER.debug("Logged user {}", user);
      return user;
    } catch (AuthenticationDeniedException | GenericException e) {
      throw new RESTException(e);
    }
  }
}
