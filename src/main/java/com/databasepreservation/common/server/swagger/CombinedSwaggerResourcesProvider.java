/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.swagger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Component
public class CombinedSwaggerResourcesProvider {
  @Primary
  @Bean
  public SwaggerResourcesProvider swaggerResourcesProvider(InMemorySwaggerResourcesProvider defaultResourcesProvider) {
    return () -> {
      SwaggerResource wsResource = new SwaggerResource();
      wsResource.setName("DBPTK Enterprise API");
      wsResource.setSwaggerVersion("1.0");
      wsResource.setLocation("/api/swagger.json");

      List<SwaggerResource> resources = new ArrayList<>();
      resources.add(wsResource);
      return resources;
    };
  }
}
