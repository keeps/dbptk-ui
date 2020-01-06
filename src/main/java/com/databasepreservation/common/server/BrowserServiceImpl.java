package com.databasepreservation.common.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.index.factory.SolrClientFactory;


@SuppressWarnings("serial")
public class BrowserServiceImpl extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  @Override
  public void init() throws ServletException {
    super.init();
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
