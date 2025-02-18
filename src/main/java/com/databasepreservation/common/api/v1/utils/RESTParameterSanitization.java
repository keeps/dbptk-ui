package com.databasepreservation.common.api.v1.utils;

public class RESTParameterSanitization {

  public static void sanitizePath(String path, String errorMessage) throws IllegalArgumentException {
    if (path.contains("..") || path.contains("/") || path.contains("\\")) {
      throw new IllegalArgumentException(errorMessage);
    }
  }


  private RESTParameterSanitization() {
    // do nothing
  }
}
