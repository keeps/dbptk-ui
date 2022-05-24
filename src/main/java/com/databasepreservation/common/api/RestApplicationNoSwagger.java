/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.Configuration;

import com.databasepreservation.common.api.exceptions.RestExceptionMapper;
import com.databasepreservation.common.api.utils.CacheFilterFactory;
import com.databasepreservation.common.api.v1.ActivityLogResource;
import com.databasepreservation.common.api.v1.AuthenticationResource;
import com.databasepreservation.common.api.v1.ClientLoggerResource;
import com.databasepreservation.common.api.v1.CollectionResource;
import com.databasepreservation.common.api.v1.ContextResource;
import com.databasepreservation.common.api.v1.DatabaseResource;
import com.databasepreservation.common.api.v1.FileResource;
import com.databasepreservation.common.api.v1.JobResource;
import com.databasepreservation.common.api.v1.MigrationResource;
import com.databasepreservation.common.api.v1.SiardResource;
import com.databasepreservation.common.api.v1.ThemeResource;

import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class RestApplicationNoSwagger extends ResourceConfig {
  public RestApplicationNoSwagger() {
    super();
    property(ServletProperties.FILTER_FORWARD_ON_404, true);
    property(ServletProperties.FILTER_CONTEXT_PATH, "/api/*");

    OpenAPI oas = new OpenAPI();
    Info info = new Info().title("DBPTK Enterprise API").description("REST API for the DBPTK Enterprise")
      .contact(new Contact().email("info@keep.pt").name("KEEP SOLUTIONS")
        .url("https://www.keep.pt/en/contacts-proposals-information-telephone-address"))
      .license(new License().name("LGPLv3").url("http://www.gnu.org/licenses/lgpl-3.0.html")).version("1.0.0");

    oas.info(info);

    // if (StringUtils.isNotBlank(context.getContextPath())) {
    // oas.addServersItem(new Server().url(context.getContextPath()));
    // }

    OpenApiResource openApiResource = new OpenApiResource();
    SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas).prettyPrint(true)
      .resourcePackages(Stream.of("com.databasepreservation.visualization.api").collect(Collectors.toSet()));
    openApiResource.setOpenApiConfiguration(oasConfig);
    register(openApiResource);
    register(JacksonFeature.class);
    register(MoxyXmlFeature.class);
    register(MultiPartFeature.class);
    register(RestExceptionMapper.class);
    register(CacheFilterFactory.class);

    register(ActivityLogResource.class);
    register(AuthenticationResource.class);
    register(ClientLoggerResource.class);
    register(CollectionResource.class);
    register(ContextResource.class);
    register(DatabaseResource.class);
    register(FileResource.class);
    register(JobResource.class);
    register(MigrationResource.class);
    register(SiardResource.class);
    register(ThemeResource.class);
    register(SwaggerSerializers.class);

    // packages("com.databasepreservation.visualization.api","com.databasepreservation.common.client.services");
    // packages("io.swagger.v3.jaxrs2.integration.resources");
    // register(CorsFilter.class);
  }
}
