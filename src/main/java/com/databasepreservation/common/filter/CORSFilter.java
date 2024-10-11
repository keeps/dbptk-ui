package com.databasepreservation.common.filter;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.databasepreservation.common.server.ViewerConfiguration;

@Provider
public class CORSFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {

    ViewerConfiguration configuration = ViewerConfiguration.getInstance();

    List<String> allowedOriginsList = configuration.getViewerConfigurationAsList(ViewerConfiguration.CORS_ALLOW_ORIGIN);
    String requestOrigin = request.getHeaderString("Origin");

    if (allowedOriginsList.contains(requestOrigin)) {
      response.getHeaders().add("Access-Control-Allow-Origin", requestOrigin);
    }
    response.getHeaders().add("Access-Control-Allow-Headers",
      configuration.getViewerConfigurationAsString("", ViewerConfiguration.CORS_ALLOW_HEADERS));
    response.getHeaders().add("Access-Control-Allow-Credentials",
      configuration
      .getViewerConfigurationAsString("false", ViewerConfiguration.CORS_ALLOW_CREDENTIALS));
    response.getHeaders().add("Access-Control-Allow-Methods",
      configuration.getViewerConfigurationAsString("", ViewerConfiguration.CORS_ALLOW_METHODS));

  }

}