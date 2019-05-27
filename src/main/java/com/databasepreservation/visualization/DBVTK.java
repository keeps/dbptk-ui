package com.databasepreservation.visualization;

import com.databasepreservation.visualization.server.BrowserServiceImpl;
import com.databasepreservation.visualization.server.ViewerConfiguration;
import com.databasepreservation.visualization.shared.server.ClientLoggerImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
public class DBVTK{

  public static void main(String[] args) {
    ViewerConfiguration.getInstance();
    SpringApplication.run(DBVTK.class, args);
  }

  @Bean
  public ServletRegistrationBean browseService() {
    ServletRegistrationBean bean = new ServletRegistrationBean(
            new BrowserServiceImpl(), "/com.databasepreservation.visualization.Viewer/browse");
    return bean;
  }

  @Bean
  public ServletRegistrationBean clientLogger() {
    ServletRegistrationBean bean = new ServletRegistrationBean(
            new ClientLoggerImpl(), "/com.databasepreservation.visualization.Viewer/wuilogger");
    bean.setLoadOnStartup(2);
    return bean;
  }


}
