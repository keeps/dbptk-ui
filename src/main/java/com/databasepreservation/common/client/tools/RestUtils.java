/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.tools;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.utils.UriQueryUtils;
import com.databasepreservation.common.client.index.FindRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class RestUtils {

  private RestUtils() {
    // do nothing
  }

  public static SafeUri createReportResourceUri(String databaseUUID) {
    //api/v1/database/{databaseUUID}/collection/{collectionUUID}/report
    String b = ViewerConstants.API_SERVLET + ViewerConstants.API_V1_DATABASE_RESOURCE + ViewerConstants.API_SEP
      + databaseUUID + ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_SOLR_COLLECTION + ViewerConstants.API_SEP
        + databaseUUID + ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_REPORT;
    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createFileResourceDownloadSIARDUri(String filename) {
    // api/v1/file/download/siard/
    String b = ViewerConstants.API_SERVLET + ViewerConstants.API_V1_FILE_RESOURCE + ViewerConstants.API_SEP
      + ViewerConstants.API_PATH_PARAM_DOWNLOAD + ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_SIARD
      + ViewerConstants.API_QUERY_START + ViewerConstants.API_PATH_PARAM_FILENAME
      + ViewerConstants.API_QUERY_ASSIGN_SYMBOL + filename;
    return UriUtils.fromSafeConstant(b);
  }

  public static SafeUri createFileResourceDownloadValidationReportUri(String databaseUUID) {
    // api/v1/database/{databaseUUID}/siard/{siardUUID}/validation
    String b = ViewerConstants.API_SERVLET + ViewerConstants.API_V1_DATABASE_RESOURCE + ViewerConstants.API_SEP
      + databaseUUID + ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_SIARD + ViewerConstants.API_SEP
      + databaseUUID + ViewerConstants.API_SEP + ViewerConstants.API_PATH_PARAM_DOWNLOAD + ViewerConstants.API_SEP
      + ViewerConstants.API_PATH_PARAM_VALIDATION_REPORT;
    return UriUtils.fromSafeConstant(b);
  }

  public static String createExportLobUri(String databaseUUID, String schemaName, String tableName, String rowIndex, int columnIndex, String filename) {
    // api/v1/database/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{row index}/{col index}
    StringBuilder urlBuilder = new StringBuilder();

    urlBuilder.append(GWT.getHostPageBaseURL()).append(ViewerConstants.API_SERVLET)
        .append(ViewerConstants.API_V1_DATABASE_RESOURCE).append("/").append(databaseUUID).append("/collection/")
        .append(databaseUUID).append("/data/").append(schemaName).append("/").append(tableName).append("/")
        .append(rowIndex).append("/").append(columnIndex);

    String queryStart = "?";
    urlBuilder.append(queryStart);
    urlBuilder.append(ViewerConstants.API_PATH_PARAM_LOB_FILENAME).append("=")
          .append(UriQueryUtils.encodeQuery(filename));

    return urlBuilder.toString();
  }

  public static String createExportTableUri(String databaseUUID, String schemaName, String tableName,
                                            FindRequest findRequest, String zipFilename, String filename, boolean descriptions, boolean lobs, List<String> fieldsToHeader) {
    // api/v1/database/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/find/export
    return exportMultiRowCSV(databaseUUID, schemaName, tableName, findRequest, zipFilename, filename, descriptions, lobs, fieldsToHeader);
  }

  public static String createExportRowUri(String databaseUUID, String schemaName, String tableName, String rowIndex, String zipFilename, String filename, boolean descriptions, boolean lobs) {
    // api/v1/database/{databaseUUID}/collection/{collectionUUID}/data/{schema}/{table}/{rowIndex}/export
    return exportSingleRowCSV(databaseUUID, schemaName, tableName, rowIndex, zipFilename, filename, descriptions, lobs);
  }

  private static String exportSingleRowCSV(String databaseUUID, String schemaName, String tableName, String rowIndex,
    String zipFilename, String filename, boolean descriptions, boolean lobs) {
    StringBuilder urlBuilder = new StringBuilder();

    urlBuilder.append(GWT.getHostPageBaseURL()).append(ViewerConstants.API_SERVLET)
      .append(ViewerConstants.API_V1_DATABASE_RESOURCE).append("/").append(databaseUUID).append("/collection/")
      .append(databaseUUID).append("/data/").append(schemaName).append("/").append(tableName).append("/")
      .append(rowIndex).append("/export");

    return getCollectionResourceExportCSVUri(urlBuilder, null, zipFilename, filename, descriptions, lobs, new ArrayList<>());
  }

  private static String exportMultiRowCSV(String databaseUUID, String schemaName, String tableName,
    FindRequest findRequest, String zipFilename, String filename, boolean descriptions, boolean lobs, List<String> fieldsToHeader) {
    StringBuilder urlBuilder = new StringBuilder();

    urlBuilder.append(GWT.getHostPageBaseURL()).append(ViewerConstants.API_SERVLET)
      .append(ViewerConstants.API_V1_DATABASE_RESOURCE).append("/").append(databaseUUID).append("/collection/")
      .append(databaseUUID).append("/data/").append(schemaName).append("/").append(tableName).append("/find/export");

    return getCollectionResourceExportCSVUri(urlBuilder, findRequest, zipFilename, filename, descriptions, lobs, fieldsToHeader);
  }

  private static String getCollectionResourceExportCSVUri(StringBuilder header, FindRequest findRequest,
    String zipFilename, String filename, boolean descriptions, boolean lobs, List<String> fieldsToHeader) {
    String paramFindRequest = null;
    if (findRequest != null) {
      paramFindRequest = ViewerJsonUtils.getFindRequestMapper().write(findRequest);
    }

    String queryStart = "?";
    header.append(queryStart);
    if (paramFindRequest != null) {
      header.append(ViewerConstants.API_QUERY_PARAM_FILTER).append("=")
        .append(UriQueryUtils.encodeQuery(paramFindRequest)).append("&");
    }
    header.append("filename").append("=").append(UriQueryUtils.encodeQuery(filename)).append("&");
    if (lobs) {
      header.append("zipFilename").append("=").append(UriQueryUtils.encodeQuery(zipFilename)).append("&");
    }
    header.append("descriptions").append("=").append(descriptions).append("&");
    header.append("lobs").append("=").append(lobs).append("&");

    if (!fieldsToHeader.isEmpty()) {
      header.append("fl").append("=").append(UriQueryUtils.encodeQuery(String.join(",", fieldsToHeader)));
    }

    return header.toString();
  }

  public static SafeUri createThemeResourceUri(String resourceId, String defaultResourceId, boolean inline) {
    // api/v1/theme/?resource_id={resourceId}&default_resource_od={defaultResourceId}
    StringBuilder b = new StringBuilder();

    b.append(ViewerConstants.API_SERVLET).append(ViewerConstants.API_V1_THEME_RESOURCE).append(ViewerConstants.API_SEP).append(ViewerConstants.API_QUERY_START)
        .append(ViewerConstants.API_QUERY_PARAM_RESOURCE_ID).append(ViewerConstants.API_QUERY_ASSIGN_SYMBOL)
        .append(resourceId);

    if (defaultResourceId != null) {
      b.append(ViewerConstants.API_QUERY_SEP).append(ViewerConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID)
          .append(ViewerConstants.API_QUERY_ASSIGN_SYMBOL).append(defaultResourceId);
    }

    if (inline) {
      b.append(ViewerConstants.API_QUERY_SEP).append(ViewerConstants.API_QUERY_PARAM_INLINE)
          .append(ViewerConstants.API_QUERY_ASSIGN_SYMBOL).append(inline);
    }

    return UriUtils.fromSafeConstant(b.toString());
  }
}
