package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.ViewerConstants;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportResourcesUtils {

  public static String getExportURL(final String databaseUUID, final String tableUUID, final String paramFilter,
    final String paramFieldList, final String paramSubList, final String paramSorter, String zipFilename,
    String filename, boolean description, boolean exportLobs) {
    // builds something like
    // http://hostname:port/api/v1/exports/csv/databaseUUID?
    StringBuilder urlBuilder = new StringBuilder();
    String base = com.google.gwt.core.client.GWT.getHostPageBaseURL();
    String servlet = ViewerConstants.API_SERVLET;
    String resource = ViewerConstants.API_V1_EXPORT_RESOURCE;
    String method = "/csv/";
    String queryStart = "?";
    urlBuilder.append(base).append(servlet).append(resource).append(method).append(databaseUUID).append(queryStart);

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
}
