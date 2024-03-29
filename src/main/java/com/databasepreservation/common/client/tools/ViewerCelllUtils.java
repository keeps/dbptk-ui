/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.tools;

import com.databasepreservation.common.client.ViewerConstants;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewerCelllUtils {

  public static String getRowIndexTemplate() {
    return ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_LOB_ROW_INDEX
      + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
  }

  public static String getColIndexTemplate() {
    return ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_LOB_COLUMN_INDEX
      + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
  }

  public static String getAutoDetectedExtensionTemplate() {
    return ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_EXTENSION
      + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
  }

  public static String getAutoDetectMimeTypeTemplate() {
    return ViewerConstants.OPEN_TEMPLATE_ENGINE
      + ViewerStringUtils.replaceAllFor(ViewerConstants.TEMPLATE_LOB_AUTO_DETECTED_MIME_TYPE, "\\s", "_")
      + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
  }

  public static String getMimeTypeSolrName(String solrColumnName) {
    return solrColumnName + getMimeTypeSuffix();
  }

  public static String getMimeTypeSuffix() {
    return ViewerConstants.SOLR_DYN_MIMETYPE + ViewerConstants.SOLR_DYN_TEXT_GENERAL;
  }

  public static String getFileExtensionSolrName(String solrColumnName) {
    return solrColumnName + getFileExtensionSuffix();
  }

  public static String getFileExtensionSuffix() {
    return ViewerConstants.SOLR_DYN_FILE_EXTENSION + ViewerConstants.SOLR_DYN_TEXT_GENERAL;
  }

  public static String getStoreTypeSolrColumnName(String solrColumnName) {
    return solrColumnName + ViewerConstants.SOLR_DYN_LOB_STORE_TYPE + ViewerConstants.SOLR_DYN_TEXT_GENERAL;
  }

  public static String getStoreTypeColumnSuffix() {
    return ViewerConstants.SOLR_DYN_LOB_STORE_TYPE + ViewerConstants.SOLR_DYN_TEXT_GENERAL;
  }
}
