/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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
