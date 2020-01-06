package com.databasepreservation.common.server.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Configuration("swaggerConfigProperties")
public class SwaggerConfigProperties {
  @Value("${api.version}")
  private String apiVersion;
  @Value("${swagger.enabled}")
  private String enabled = "false";
  @Value("${swagger.title}")
  private String title;
  @Value("${swagger.description}")
  private String description;
  @Value("${swagger.useDefaultResponseMessages}")
  private String useDefaultResponseMessages;
  @Value("${swagger.enableUrlTemplating}")
  private String enableUrlTemplating;
  @Value("${swagger.deepLinking}")
  private String deepLinking;
  @Value("${swagger.defaultModelsExpandDepth}")
  private String defaultModelsExpandDepth;
  @Value("${swagger.defaultModelExpandDepth}")
  private String defaultModelExpandDepth;
  @Value("${swagger.displayOperationId}")
  private String displayOperationId;
  @Value("${swagger.displayRequestDuration}")
  private String displayRequestDuration;
  @Value("${swagger.filter}")
  private String filter;
  @Value("${swagger.maxDisplayedTags}")
  private String maxDisplayedTags;
  @Value("${swagger.showExtensions}")
  private String showExtensions;

  public String getApiVersion() {
    return apiVersion;
  }

  public String getEnabled() {
    return enabled;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getUseDefaultResponseMessages() {
    return useDefaultResponseMessages;
  }

  public String getEnableUrlTemplating() {
    return enableUrlTemplating;
  }

  public String getDeepLinking() {
    return deepLinking;
  }

  public String getDefaultModelsExpandDepth() {
    return defaultModelsExpandDepth;
  }

  public String getDefaultModelExpandDepth() {
    return defaultModelExpandDepth;
  }

  public String getDisplayOperationId() {
    return displayOperationId;
  }

  public String getDisplayRequestDuration() {
    return displayRequestDuration;
  }

  public String getFilter() {
    return filter;
  }

  public String getMaxDisplayedTags() {
    return maxDisplayedTags;
  }

  public String getShowExtensions() {
    return showExtensions;
  }
}
