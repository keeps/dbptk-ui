/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.apereo.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.filter.OnOffFilter;
import com.databasepreservation.common.server.BrowserServiceImpl;
import com.databasepreservation.common.server.ViewerConfiguration;

import jakarta.servlet.http.HttpServlet;

@SpringBootApplication
public class DBVTK {
  public static void main(String[] args) {
    ViewerConfiguration.getInstance();
    SpringApplication.run(DBVTK.class, args);
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> browserService() {
    ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>();
    bean.setServlet(new BrowserServiceImpl());
    if (ViewerConstants.APPLICATION_ENV_DESKTOP
      .equals(System.getProperty(ViewerConstants.APPLICATION_ENV_KEY, ViewerConstants.APPLICATION_ENV_SERVER))) {
      bean.addUrlMappings("/com.databasepreservation.desktop.Desktop/browse");
    } else {
      bean.addUrlMappings("/com.databasepreservation.server.Server/browse");
    }
    bean.setLoadOnStartup(2);
    return bean;
  }

  @Bean
  public ApplicationListener<ServletWebServerInitializedEvent> getPort() {
    return new ApplicationListener<ServletWebServerInitializedEvent>() {
      @Override
      public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        if (System.getProperty("server.port", "").equals("0")) {
          // Using a Random Unassigned HTTP Port
          int port = event.getWebServer().getPort();
          String portFilePath = System.getProperty("server.port.file", "");
          if (!portFilePath.isEmpty()) {
            Path portFile = Paths.get(portFilePath);
            try {
              Files.write(portFile, Integer.toString(port).getBytes());
              System.out.println("Written port " + port + " to file " + portFile);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    };
  }

  @Bean
  public RequestContextFilter requestContextFilter() {
    OrderedRequestContextFilter filter = new OrderedRequestContextFilter();
    filter.setOrder(-100001);
    return filter;
  }

  /*********************
   * Authentication
   *********************/
  @Bean
  public FilterRegistrationBean<OnOffFilter> internalWebAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("InternalWebAuthFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "com.databasepreservation.common.filter.InternalWebAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.internal");
    registrationBean.addUrlPatterns("/login", "/logout");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> internalApiAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("InternalApiAuthFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "com.databasepreservation.common.filter.InternalApiAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.internal");

    // Realm to be used
    registrationBean.addInitParameter("realm", "RODA REST API");

    // Comma separated list of relative paths to exclude in filter logic (using
    // regular expressions for extra power)
    registrationBean.addInitParameter("exclusions", "^/openapi.json,^/v1/theme/?");

    registrationBean.addUrlPatterns("/api/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casSingleSignOutFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasSingleSignOutFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.apereo.cas.client.session.SingleSignOutFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "http://localhost:8888/cas");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public SingleSignOutHttpSessionListener singleSignOutHttpSessionListener() {
    return new SingleSignOutHttpSessionListener();
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casValidationFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasValidationFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "org.apereo.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "https://localhost:8443/cas");
    registrationBean.addInitParameter("serverName", "https://localhost:8888");
    registrationBean.addInitParameter("exceptionOnValidationFailure", "false");
    registrationBean.addInitParameter("redirectAfterValidation", "false");
    registrationBean.addInitParameter("proxyCallbackUrl", "");
    registrationBean.addInitParameter("proxyReceptorUrl", "");
    registrationBean.addInitParameter("acceptAnyProxy", "false");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casAuthenticationFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasAuthenticationFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.apereo.cas.client.authentication.AuthenticationFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerLoginUrl", "https://localhost:8443/cas/login");
    registrationBean.addUrlPatterns("/login");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casRequestWrapperFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasRequestWrapperFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "org.apereo.cas.client.util.HttpServletRequestWrapperFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casApiAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasApiAuthFilter");
    registrationBean.addInitParameter("inner-filter-class", "com.databasepreservation.common.filter.CasApiAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "https://localhost:8443/cas");
    registrationBean.addInitParameter("exclusions", "^/swagger.json,^/v1/theme/?,^/v1/auth/ticket?");
    registrationBean.addUrlPatterns("/api/v1/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casWebAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasWebAuthFilter");
    registrationBean.addInitParameter("inner-filter-class", "com.databasepreservation.common.filter.CasWebAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerLogoutUrl", "https://localhost:8443/cas/logout");

    registrationBean.addUrlPatterns("/login", "/logout");

    return registrationBean;
  }

  /*********************
   * H2 Datasource
   *********************/
  @Bean
  public DataSource dataSource() {
    DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    dataSourceBuilder.driverClassName("org.h2.Driver");
    dataSourceBuilder
      .url("jdbc:h2:file:" + ViewerConfiguration.getInstance().getH2Path().normalize().toAbsolutePath().toString());
    dataSourceBuilder.username("sa");
    dataSourceBuilder.password("");
    return dataSourceBuilder.build();
  }

  @Configuration
  public static class DefaultView implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      if (ViewerConstants.APPLICATION_ENV_DESKTOP
        .equals(System.getProperty(ViewerConstants.APPLICATION_ENV_KEY, ViewerConstants.APPLICATION_ENV_SERVER))) {
        registry.addViewController("/").setViewName("forward:/desktop.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
      }
      registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/com.databasepreservation.server.Server/**")
        .addResourceLocations("classpath:/com.databasepreservation.server.Server/");

      if (ViewerConstants.APPLICATION_ENV_DESKTOP
        .equals(System.getProperty(ViewerConstants.APPLICATION_ENV_KEY, ViewerConstants.APPLICATION_ENV_SERVER))) {
        registry.addResourceHandler("/com.databasepreservation.desktop.Desktop/**")
          .addResourceLocations("classpath:/com.databasepreservation.desktop.Desktop/");
      } else {
        registry.addResourceHandler("/com.databasepreservation.server.Server/**")
          .addResourceLocations("classpath:/com.databasepreservation.server.Server/");
      }
    }
  }



  // @Bean
  // MultipartConfigElement multipartConfigElement() {
  // MultipartConfigFactory factory = new MultipartConfigFactory();
  // factory.setMaxFileSize(DataSize.ofBytes(512000000L));
  // factory.setMaxRequestSize(DataSize.ofBytes(512000000L));
  // return factory.createMultipartConfig();
  // }
}