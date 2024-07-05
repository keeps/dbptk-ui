/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.AuthenticationService;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.utils.UserUtility;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_AUTHENTICATION)
public class AuthenticationResource implements AuthenticationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

  @Autowired
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
