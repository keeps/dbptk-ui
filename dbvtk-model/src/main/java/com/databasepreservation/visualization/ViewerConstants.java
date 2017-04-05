package com.databasepreservation.visualization;

/**
 * Non GWT-safe constants used in Database Viewer.
 *
 * @see com.databasepreservation.visualization.shared.ViewerSafeConstants for
 *      the GWT-safe constants
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {
  /*
   * DBVTK CONFIG
   */
  public static final String INSTALL_FOLDER_SYSTEM_PROPERTY = "dbvtk.home";
  public static final String INSTALL_FOLDER_ENVIRONMENT_VARIABLE = "DBVTK_HOME";
  public static final String INSTALL_FOLDER_DEFAULT_SUBFOLDER_UNDER_HOME = ".dbvtk";

  public static final String DEFAULT_ENCODING = "UTF-8";
  public static final String VIEWER_CONFIG_FOLDER = "config";
  public static final String VIEWER_I18N_CLIENT_FOLDER = "client";
  public static final String VIEWER_I18_GWT_XML_FILE = "I18N.gwt.xml";
  public static final String VIEWER_EXAMPLE_CONFIG_FOLDER = "example-config";
  public static final String VIEWER_I18N_FOLDER = "i18n";
  public static final String VIEWER_THEME_FOLDER = "theme";
  public static final String VIEWER_LOG_FOLDER = "log";
  public static final String VIEWER_LOBS_FOLDER = "lobs";
  public static final String VIEWER_UPLOADS_FOLDER = "uploads";
  public static final String VIEWER_REPORTS_FOLDER = "reports";

  public final static String MEDIA_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
  public final static String MEDIA_TYPE_TEXT_HTML = "text/html";
}
