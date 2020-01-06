package com.databasepreservation.common.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RestApplication extends ResourceConfig {
  private static final String DBVTK_API_PACKAGE = "com.databasepreservation.common.api";
  private static final String SWAGGER_PACKAGE = "io.swagger.jaxrs.listing";

  public RestApplication() {
    super();
    packages("com.databasepreservation.common.api", "com.databasepreservation.common.client.services",
      ApiListingResource.class.getPackage().getName());

    register(JacksonFeature.class);
    register(MoxyXmlFeature.class);
    register(MultiPartFeature.class);

    // https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-Jersey-2.X-Project-Setup-1.5
    register(ApiListingResource.class);
    register(SwaggerSerializers.class);
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1");
    beanConfig.setBasePath("/api");
    beanConfig
      .setResourcePackage("com.databasepreservation.common.api,com.databasepreservation.common.client.services");
    beanConfig.setScan(true);
    SwaggerConfigLocator.getInstance().putConfig(SwaggerContextService.CONFIG_ID_DEFAULT, beanConfig);
  }
}
