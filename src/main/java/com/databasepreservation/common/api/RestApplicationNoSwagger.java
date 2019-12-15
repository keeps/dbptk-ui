package com.databasepreservation.common.api;

import io.swagger.models.Contact;
import io.swagger.models.License;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.context.annotation.Configuration;

import com.databasepreservation.common.api.exceptions.RestExceptionMapper;
import com.databasepreservation.common.api.v1.ActivityLogResource;
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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Info;

@Configuration
public class RestApplicationNoSwagger {

  @Configuration
  static class JerseyConfig extends ResourceConfig {
    /**
     * We just need the packages that have REST resources in them.
     */
    public JerseyConfig() {
      property(ServletProperties.FILTER_FORWARD_ON_404, true);
      property(ServletProperties.FILTER_CONTEXT_PATH, "/api/*");
      // packages("com.databasepreservation.visualization.api");

      register(ActivityLogResource.class);
      register(AuthenticationResource.class);
      register(ConfigurationResource.class);
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

      register(MyApiListingResource.class);
      register(SwaggerSerializers.class);

      BeanConfig beanConfig = new BeanConfig();
      beanConfig.setVersion("1");
      // beanConfig.setBasePath("");
      beanConfig.setResourcePackage("com.databasepreservation.common.api");
      beanConfig.setScan(true);
      Info info = new Info();
      info.setTitle("DBPTK Enterprise API");
      info.setDescription("REST API for the DBPTK Enterprise");
      License license = new License();
      Contact contact = new Contact().email("info@keep.pt").url("https://www.keep.pt/en/contacts-proposals-information-telephone-address/").name("Keep Solutions");
      info.setContact(contact);
      license.name("LGPLv3").setUrl("http://www.gnu.org/licenses/lgpl-3.0.html");
      info.setLicense(license);

      beanConfig.setInfo(info);
      SwaggerConfigLocator.getInstance().putConfig(SwaggerContextService.CONFIG_ID_DEFAULT, beanConfig);
    }
  }

}
