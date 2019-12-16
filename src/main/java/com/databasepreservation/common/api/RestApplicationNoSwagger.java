package com.databasepreservation.common.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.Configuration;

import com.databasepreservation.common.api.exceptions.RestExceptionMapper;
import com.databasepreservation.common.api.v1.AuthenticationResource;
import com.databasepreservation.common.api.v1.ClientLoggerResource;
import com.databasepreservation.common.api.v1.ContextResource;
import com.databasepreservation.common.api.v1.DatabaseResource;
import com.databasepreservation.common.api.v1.ExportsResource;
import com.databasepreservation.common.api.v1.FileResource;
import com.databasepreservation.common.api.v1.LobsResource;
import com.databasepreservation.common.api.v1.ModulesResource;
import com.databasepreservation.common.api.v1.ReportResource;
import com.databasepreservation.common.api.v1.SIARDResource;
import com.databasepreservation.common.api.v1.SearchResource;
import com.databasepreservation.common.api.v1.ThemeResource;

@Configuration
public class RestApplicationNoSwagger {

  @Configuration
  static class JerseyConfig extends ResourceConfig {
    /**
     * We just need the packages that have REST resources in them.
     */
    public JerseyConfig() {
      property(ServletProperties.FILTER_FORWARD_ON_404, true);
      property(ServletProperties.FILTER_CONTEXT_PATH,"/api/*");
      //packages("com.databasepreservation.visualization.api");

      register(AuthenticationResource.class);
      register(ContextResource.class);
      register(DatabaseResource.class);
      register(ExportsResource.class);
      register(FileResource.class);
      register(LobsResource.class);
      register(ModulesResource.class);
      register(ReportResource.class);
      register(SearchResource.class);
      register(SIARDResource.class);
      register(ClientLoggerResource.class);
      register(ThemeResource.class);
      register(JacksonFeature.class);
      register(MoxyXmlFeature.class);
      register(RestExceptionMapper.class);
      register(MultiPartFeature.class);
    }
  }
}
