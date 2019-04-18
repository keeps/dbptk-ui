/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.shared.client.Tools;

import com.databasepreservation.visualization.shared.ViewerConstants;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class RestUtils {

  private RestUtils() {
    // do nothing
  }

  public static SafeUri createReportResourceUri(String databaseUUID) {
    // api/v1/report/{databaseUUID}
    StringBuilder b = new StringBuilder().append(ViewerConstants.API_SERVLET)
      .append(ViewerConstants.API_V1_REPORT_RESOURCE).append(ViewerConstants.API_SEP).append(databaseUUID);
    return UriUtils.fromSafeConstant(b.toString());
  }
}
