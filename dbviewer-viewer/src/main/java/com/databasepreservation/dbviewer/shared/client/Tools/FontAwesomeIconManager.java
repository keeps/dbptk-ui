package com.databasepreservation.dbviewer.shared.client.Tools;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class FontAwesomeIconManager {
  public static final String DATABASES = "server";
  public static final String DATABASE = "database";
  public static final String SCHEMA = "cube";
  public static final String TABLE = "table";
  public static final String RECORD = "file-o";
  public static final String REFERENCE = "exchange";
  public static final String DATABASE_INFORMATION = "info-circle";
  public static final String DATABASE_USERS = "users";
  public static final String SCHEMA_STRUCTURE = "sitemap";
  public static final String SCHEMA_ROUTINES = "cog";
  public static final String SCHEMA_TRIGGERS = "clock-o";
  public static final String SCHEMA_VIEWS = "filter";
  public static final String SCHEMA_DATA = "th-large";
  public static final String SCHEMA_CHECK_CONSTRAINTS = "compress";

  public static String getTag(String icon) {
    return "<i class=\"fa fa-" + icon + "\"></i>";
  }

  public static String getTag(String icon, String tooltip) {
    return "<i class=\"fa fa-" + icon + "\"></i>";
  }
}
