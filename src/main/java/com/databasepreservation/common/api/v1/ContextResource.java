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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.exceptions.AuthorizationException;
import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroupsList;
import com.databasepreservation.common.client.services.ContextService;
import com.databasepreservation.common.server.ServerTools;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.utils.ControllerAssistant;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_CONTEXT)
public class ContextResource implements ContextService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContextResource.class);

  @Autowired
  private HttpServletRequest request;

  @Override
  public StringResponse getEnvironment() {
    return new StringResponse(ViewerFactory.getViewerConfiguration().getApplicationEnvironment());
  }

  @Override
  public StringResponse getClientMachine() {
    try {
      return new StringResponse(InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      LOGGER.debug("UnknownHostException");
    }
    return new StringResponse("");
  }

  @Override
  public Map<String, List<String>> getSharedProperties(String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return ViewerConfiguration.getSharedProperties(locale);
  }

  @Override
  public Set<AuthorizationGroup> getAuthorizationGroupsList() {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    try {
      controllerAssistant.checkRoles(request);
    } catch (AuthorizationException e) {
      throw new RESTException(e);
    }
    AuthorizationGroupsList authorizationGroupsList = ViewerConfiguration.getInstance()
      .getCollectionsAuthorizationGroupsWithDefault();

    return authorizationGroupsList.getAuthorizationGroupsList().stream()
      .sorted(Comparator.comparing(AuthorizationGroup::getLabel, String.CASE_INSENSITIVE_ORDER))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
