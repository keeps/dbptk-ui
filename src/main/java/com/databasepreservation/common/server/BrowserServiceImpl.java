package com.databasepreservation.common.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;




/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class BrowserServiceImpl extends HttpServlet implements BrowserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  /**
   * Overridden to load the gwt.codeserver.port system property.
   *
   * @param config
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    new Thread(SolrClientFactory::get).start();
  }

  /**
   * Called by the servlet container to indicate to a servlet that the servlet is
   * being taken out of service.
   */
  @Override
  public void destroy() {
    super.destroy();

    try {
      SolrClientFactory.get().getSolrClient().close();
    } catch (IOException e) {
      LOGGER.error("Stopping SolrClient", e);
    }
  }
}
