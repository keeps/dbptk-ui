package com.databasepreservation.common.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.AuthenticationService;
import com.databasepreservation.common.server.ViewerConfiguration;
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
    final boolean isAuthenticationEnabled = ViewerConfiguration.getInstance().getIsAuthenticationEnabled();

    User user;

    if (isAuthenticationEnabled) {
      user = UserUtility.getUser(request);
    } else {
      user = UserUtility.getNoAuthenticationUser();
      UserUtility.setUser(request, user);
    }
    LOGGER.debug("Serving user {}", user);
    return user;
  }
}
