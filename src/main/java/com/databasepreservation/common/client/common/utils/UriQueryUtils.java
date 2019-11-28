package com.databasepreservation.common.client.common.utils;

import com.google.gwt.safehtml.shared.UriUtils;

public final class UriQueryUtils {

  private UriQueryUtils() {
  }
  
  public static String encodeQuery(String queryFieldValue) {
    // in query  chars [] cannot appear
    return UriUtils.encode(queryFieldValue).replaceAll("\\[", "%5B").replaceAll("\\]", "%5D");
  }

}
