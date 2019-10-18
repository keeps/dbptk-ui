package com.databasepreservation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.databasepreservation.common.server.BrowserServiceImpl;
import com.databasepreservation.common.server.ClientLoggerImpl;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.shared.ViewerConstants;

@SpringBootApplication
public class DBVTK {
  public static void main(String[] args) {
    ViewerConfiguration.getInstance();
    SpringApplication.run(DBVTK.class, args);
  }

  @Configuration
  public static class DefaultView implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      if (ViewerConstants.DESKTOP.equals(System.getProperty("env", "server"))) {
        registry.addViewController("/").setViewName("forward:/desktop.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
      }
    }
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> browserService() {
    ServletRegistrationBean<HttpServlet> bean;
    if (ViewerConstants.DESKTOP.equals(System.getProperty("env", "server"))) {
      bean = new ServletRegistrationBean<>(new BrowserServiceImpl(),
              "/com.databasepreservation.desktop.Desktop/browse");
    } else {
      bean = new ServletRegistrationBean<>(new BrowserServiceImpl(),
              "/com.databasepreservation.server.Server/browse");
    }
    bean.setLoadOnStartup(2);
    return bean;
  }
  @Bean
  public ServletRegistrationBean<HttpServlet> clientLogger() {
    ServletRegistrationBean<HttpServlet> bean;
    if (ViewerConstants.DESKTOP.equals(System.getProperty("env", "server"))) {
      bean = new ServletRegistrationBean<>(new ClientLoggerImpl(),
              "/com.databasepreservation.desktop.Desktop/wuilogger");
    } else {
      bean = new ServletRegistrationBean<>(new ClientLoggerImpl(),
              "/com.databasepreservation.server.Server/wuilogger");
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

//  @Bean
//  MultipartConfigElement multipartConfigElement() {
//    MultipartConfigFactory factory = new MultipartConfigFactory();
//    factory.setMaxFileSize(DataSize.ofBytes(512000000L));
//    factory.setMaxRequestSize(DataSize.ofBytes(512000000L));
//    return factory.createMultipartConfig();
//  }
}