package com.databasepreservation.common.utils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.AuthorizationException;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ControllerAssistant {
  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAssistant.class);
  private final Date startDate;
  private final Method enclosingMethod;

  public ControllerAssistant() {
    this.startDate = new Date();
    this.enclosingMethod = this.getClass().getEnclosingMethod();
  }

  public User checkWhitelistedIPs(HttpServletRequest request) {
    try {
      InetAddress address = InetAddress.getByName(request.getRemoteAddr());
      List<String> whitelistedIPs = ViewerConfiguration.getInstance().getWhitelistedIPs();
      int index = 0;
      for (String whitelistedIP : whitelistedIPs) {
        try {
          InetAddress whitelistAddress = InetAddress.getByName(whitelistedIP);
          if (Arrays.equals(address.getAddress(), whitelistAddress.getAddress())) {
            final String username = ViewerConfiguration.getInstance().getWhiteListedUsername().get(index);
            User user = new User(username);
            user.setIpAddress(address.toString());
            index++;

            return user;
          }
        } catch (UnknownHostException e) {
          LOGGER.debug("Invalid IP address from config: {}", request.getRemoteAddr(), e);
        }
      }
    } catch (UnknownHostException e) {
      LOGGER.debug("Invalid IP address: {}", request.getRemoteAddr(), e);
    }

    return null;
  }

  public User checkRoles(HttpServletRequest request) {
    if (!ViewerFactory.getViewerConfiguration().getIsAuthenticationEnabled()) {
      final User noAuthenticationUser = UserUtility.getNoAuthenticationUser();
      noAuthenticationUser.setIpAddress(request.getRemoteAddr());
      return noAuthenticationUser;
    }

    if (ViewerConfiguration.getInstance().getApplicationEnvironment().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      try {
        User user = UserUtility.getUser(request);
        UserUtility.checkRoles(user, this.getClass());
        return user;
      } catch (final AuthorizationDeniedException e) {
        final User user = checkWhitelistedIPs(request);
        if (user == null) {
          registerAction(UserUtility.getGuest(request), LogEntryState.UNAUTHORIZED);
          throw new AuthorizationException(e);
        }
        return user;
      }
    } else {
      return UserUtility.getGuest(request);
    }
  }

  public void registerAction(final User user, final String relatedObjectId, final LogEntryState state,
    final Object... parameters) {
    final long duration = new Date().getTime() - startDate.getTime();
    ControllerAssistantUtils.registerAction(user, this.enclosingMethod.getDeclaringClass().getName(),
      this.enclosingMethod.getName(), relatedObjectId, duration, state, parameters);
  }

  public void registerAction(final User user, final LogEntryState state, final Object... parameters) {
    registerAction(user, null, state, parameters);
  }

  public void registerAction(final User user, final LogEntryState state) {
    registerAction(user, (String) null, state);
  }
}
