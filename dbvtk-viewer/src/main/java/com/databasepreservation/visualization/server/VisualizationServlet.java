package com.databasepreservation.visualization.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class VisualizationServlet extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(VisualizationServlet.class);

  @Override
  public void init() throws ServletException {
    ViewerFactory.instantiate();
    LOGGER.info("Init: ok");
  }

  @Override
  public void destroy() {
    try {
      ViewerFactory.shutdown();
      LOGGER.info("Shutdown: ok");
    } catch (IOException e) {
      LOGGER.error("Error while shutting down {}", ViewerFactory.class.getName());
    }
  }
}
