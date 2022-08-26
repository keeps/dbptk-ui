/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.databasepreservation.common.client.models.authorization.AuthorizationRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.models.authorization.AuthorizationRuleList;
import com.databasepreservation.common.client.services.ContextService;
import com.databasepreservation.common.server.ServerTools;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.google.gwt.http.client.Response;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_CONTEXT)
public class ContextResource implements ContextService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContextResource.class);

  @Context
  private HttpServletRequest request;

  @Override
  public String getEnvironment() {
    return ViewerFactory.getViewerConfiguration().getApplicationEnvironment();
  }

  @Override
  public String getClientMachine() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      LOGGER.debug("UnknownHostException");
    }
    return "";
  }

  @Override
  public Map<String, List<String>> getSharedProperties(String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return ViewerConfiguration.getSharedProperties(locale);
  }

  @Override
  public Set<AuthorizationRules> getAuthorizationRuleList() {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    controllerAssistant.checkRoles(request);
    AuthorizationRuleList authorizationRuleList = ViewerConfiguration.getInstance().getCollectionsAuthorizationRules();

    return authorizationRuleList.getAuthorizationRulesList();
  }
}
