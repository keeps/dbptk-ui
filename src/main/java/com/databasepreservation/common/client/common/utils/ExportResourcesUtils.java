package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.ExportRequest;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.tools.ViewerJsonUtils;
import com.google.gwt.core.client.GWT;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportResourcesUtils {

  public static String getExportURL(final String databaseUUID, final String tableUUID, FindRequest findRequest, ExportRequest exportRequest) {
    final String paramFindRequest = ViewerJsonUtils.getFindRequestMapper().write(findRequest);
    final String paramExportRequest = ViewerJsonUtils.getExportRequestMapper().write(exportRequest);

    final StringBuilder urlBuilder = getHeader(GWT.getHostPageBaseURL(), ViewerConstants.API_SERVLET,
      ViewerConstants.API_V1_EXPORT_RESOURCE, "/csv/");

    String queryStart = "?";
    urlBuilder.append(databaseUUID).append("/").append(tableUUID).append(queryStart);

    urlBuilder.append(ViewerConstants.API_QUERY_PARAM_FILTER).append("=")
        .append(UriQueryUtils.encodeQuery(paramFindRequest)).append("&");

    urlBuilder.append(ViewerConstants.API_QUERY_PARAM_EXPORT).append("=")
        .append(UriQueryUtils.encodeQuery(paramExportRequest));

    return urlBuilder.toString();
  }

  public static String getExportURL(final String databaseUUID, final String tableUUID, final String paramFilter,
    final String paramFieldList, final String paramSubList, final String paramSorter, String zipFilename,
    String filename, boolean description, boolean exportLobs) {
    final StringBuilder urlBuilder = getHeader(GWT.getHostPageBaseURL(), ViewerConstants.API_SERVLET,
      ViewerConstants.API_V1_EXPORT_RESOURCE, "/csv/");

    String queryStart = "?";
    urlBuilder.append(databaseUUID).append(queryStart);

    urlBuilder.append(ViewerConstants.API_QUERY_PARAM_FIELDS).append("=")
      .append(UriQueryUtils.encodeQuery(paramFieldList)).append("&");

    urlBuilder.append(ViewerConstants.API_QUERY_PARAM_FILTER).append("=").append(UriQueryUtils.encodeQuery(paramFilter))
      .append("&");

    if (paramSubList != null) {
      urlBuilder.append(ViewerConstants.API_QUERY_PARAM_SUBLIST).append("=")
        .append(UriQueryUtils.encodeQuery(paramSubList)).append("&");
    }

    // add parameter: sorter
    urlBuilder.append(ViewerConstants.API_QUERY_PARAM_SORTER).append("=").append(UriQueryUtils.encodeQuery(paramSorter))
      .append("&");

    urlBuilder.append(ViewerConstants.API_PATH_PARAM_FILENAME).append("=").append(UriQueryUtils.encodeQuery(filename))
      .append("&");

    if (exportLobs) {
      urlBuilder.append(ViewerConstants.API_PATH_PARAM_ZIP_FILENAME).append("=")
        .append(UriQueryUtils.encodeQuery(zipFilename)).append("&");
    }

    urlBuilder.append(ViewerConstants.API_PATH_PARAM_EXPORT_DESCRIPTION).append("=").append(description).append("&");

    urlBuilder.append(ViewerConstants.API_PATH_PARAM_EXPORT_LOBS).append("=").append(exportLobs).append("&");

    urlBuilder.append(ViewerConstants.API_PATH_PARAM_TABLE_UUID).append("=").append(tableUUID);

    return urlBuilder.toString();
  }

  public static String getExportURL(final String databaseUUID, final String tableUUID, final String paramFilter,
    final String paramFieldList, final String paramSubList, final String paramSorter, String filename,
    boolean description) {
    return getExportURL(databaseUUID, tableUUID, paramFilter, paramFieldList, paramSubList, paramSorter, null, filename,
      description, false);
  }

  private static StringBuilder getHeader(String base, String servlet, String resource, String method) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(base).append(servlet).append(resource).append(method);

    return urlBuilder;
  }
}
