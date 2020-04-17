package com.databasepreservation.common.api.v1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Path;

import com.databasepreservation.common.server.ViewerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.services.ContextService;
import com.databasepreservation.common.server.ServerTools;
import com.databasepreservation.common.server.ViewerConfiguration;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_CONTEXT)
public class ContextResource implements ContextService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContextResource.class);

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
}
