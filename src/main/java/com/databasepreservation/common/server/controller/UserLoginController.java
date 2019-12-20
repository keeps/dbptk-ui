/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

public class UserLoginController {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginController.class);

  private UserLoginController() {
    super();
  }

  public static User login(String username, String password, HttpServletRequest request)
    throws AuthenticationDeniedException, GenericException {
    User user;

    String rodaAddress = ViewerConfiguration.getInstance()
      .getViewerConfigurationAsString(null, ViewerConfiguration.PROPERTY_RODA_ADDRESS);

    String rodaPath = ViewerConfiguration.getInstance()
      .getViewerConfigurationAsString(null, ViewerConfiguration.PROPERTY_RODA_PATH);
    rodaPath = rodaPath.replaceAll("\\{username\\}", username);

    HttpAuthenticationFeature basicAuth = HttpAuthenticationFeature.basic(username, password);
    Client client = ClientBuilder.newClient().register(basicAuth);
    WebTarget target = client.target(rodaAddress).path(rodaPath);
    try {
      user = target.request(MediaType.APPLICATION_JSON_TYPE).get(User.class);
      UserUtility.setUser(request, user);
      UserUtility.setPassword(request, password);
      return user;
    } catch (NotAuthorizedException e) {
      throw new AuthenticationDeniedException("Could not login with that username and password");
    } catch (WebApplicationException | ProcessingException e) {
      throw new GenericException("Could not authenticate with RODA", e);
    }
  }

  public static void casLogin(final String username, HttpServletRequest request) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserLoginHelper.casLogin(username, request);
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS, ViewerConstants.CONTROLLER_USERNAME_PARAM,
      username);
  }

  public static void logout(HttpServletRequest request) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    UserUtility.logout(request);
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS, ViewerConstants.CONTROLLER_USERNAME_PARAM,
      user.getName());
  }
}
