/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.tools;

import com.databasepreservation.common.client.ViewerConstants;
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

  public static SafeUri createFileResourceDownloadSIARDUri(String databaseUUID) {
    // api/v1/file/download/siard/
    StringBuilder b = new StringBuilder().append(ViewerConstants.API_SERVLET);
    b.append(ViewerConstants.API_V1_FILE_RESOURCE).append(ViewerConstants.API_SEP).append(databaseUUID)
      .append(ViewerConstants.API_SEP).append(ViewerConstants.API_PATH_PARAM_DOWNLOAD).append(ViewerConstants.API_SEP)
      .append(ViewerConstants.API_PATH_PARAM_SIARD);

    return UriUtils.fromSafeConstant(b.toString());
  }

  public static SafeUri createFileResourceDownloadValidationReportUri(String databaseUUID) {
    // api/v1/database/{databaseUUID}/download/validation/
    StringBuilder b = new StringBuilder().append(ViewerConstants.API_SERVLET);
    b.append(ViewerConstants.API_V1_DATABASE_RESOURCE).append(ViewerConstants.API_SEP).append(databaseUUID)
      .append(ViewerConstants.API_SEP).append(ViewerConstants.API_PATH_PARAM_DOWNLOAD).append(ViewerConstants.API_SEP)
      .append(ViewerConstants.API_PATH_PARAM_VALIDATION_REPORT);

    return UriUtils.fromSafeConstant(b.toString());
  }
}
