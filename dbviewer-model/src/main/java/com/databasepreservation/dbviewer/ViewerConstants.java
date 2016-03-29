package com.databasepreservation.dbviewer;

/**
 * Constants used in Database Viewer
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {
  /*
   * INDEX NAMES
   */
  public static final String SOLR_INDEX_DATABASE = "dblist";

  /*
   * DATABASE FIELDS
   */
  public static final String SOLR_DATABASE_ID = "id";
  public static final String SOLR_DATABASE_METADATA = "metadata";

  /*
   * Misc
   */
  public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String SOLRDATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final String SHA1 = "SHA-1";
  public static final String SHA256 = "SHA-256";
  public static final String MD5 = "MD5";

  /**
   * private constructor
   */
  private ViewerConstants() {
  }
}
