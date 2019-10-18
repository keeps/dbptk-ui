package com.databasepreservation.common.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.Configuration;

import com.databasepreservation.common.api.v1.*;

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

      register(ExportsResource.class);
      register(LobsResource.class);
      register(ManageResource.class);
      register(ReportResource.class);
      register(ThemeResource.class);
      register(FileResource.class);
      register(JacksonFeature.class);
      register(MoxyXmlFeature.class);
      register(MultiPartFeature.class);
    }
  }
}
