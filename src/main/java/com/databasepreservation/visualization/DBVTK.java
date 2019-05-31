package com.databasepreservation.visualization;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServlet;

import com.databasepreservation.visualization.server.BrowserServiceImpl;
import com.databasepreservation.visualization.server.ViewerConfiguration;
import com.databasepreservation.visualization.shared.server.ClientLoggerImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DBVTK {

  public static void main(String[] args) {
    ViewerConfiguration.getInstance();
    SpringApplication.run(DBVTK.class, args);
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> browseService() {
    ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(new BrowserServiceImpl(),
        "/com.databasepreservation.visualization.Viewer/browse");
    return bean;
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> clientLogger() {
    ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(new ClientLoggerImpl(),
        "/com.databasepreservation.visualization.Viewer/wuilogger");
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

}
