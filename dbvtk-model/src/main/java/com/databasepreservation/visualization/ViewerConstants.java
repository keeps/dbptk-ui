package com.databasepreservation.visualization;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constants used in Database Viewer
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {
  /*
   * CONFIGSETS
   */
  public static final String SOLR_CONFIGSET_DATABASE_RESOURCE = "solr-configset/dbvtk_database";
  public static final String SOLR_CONFIGSET_TABLE_RESOURCE = "solr-configset/dbvtk_table";
  public static final String SOLR_CONFIGSET_DATABASE = "dbvtk_database";
  public static final String SOLR_CONFIGSET_TABLE = "dbvtk_table";

  /*
   * COLLECTION STRUCTURE
   */
  public static final String SOLR_INDEX_DATABASE_COLLECTION_NAME = "dbv-database";
  public static final String SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX = "dbv-table-";
  public static final String SOLR_INDEX_ROW_COLUMN_NAME_PREFIX = "col";

  /*
   * DATABASE FIELDS
   */
  public static final String SOLR_DATABASE_ID = "id";
  public static final String SOLR_DATABASE_METADATA = "metadata";

  /*
   * ROW FIELDS
   */
  public static final String SOLR_ROW_ID = "id";
  public static final String SOLR_ROW_SEARCH = "search";

  /*
   * DYNAMIC FIELD TYPES (suffixes)
   */
  // indexed, stored
  public static final String SOLR_DYN_BOOLEAN = "_b";
  public static final String SOLR_DYN_DATE = "_dt";
  public static final String SOLR_DYN_DOUBLE = "_d";
  public static final String SOLR_DYN_FLOAT = "_f";
  public static final String SOLR_DYN_INT = "_i";
  public static final String SOLR_DYN_LOCATION = "_p";
  public static final String SOLR_DYN_LOCATION_RPT = "_srpt";
  public static final String SOLR_DYN_LONG = "_l";
  public static final String SOLR_DYN_STRING = "_s";
  public static final String SOLR_DYN_TEXT_GENERAL = "_t";

  // indexed, stored, multiValued
  public static final String SOLR_DYN_BOOLEANS = "_bs";
  public static final String SOLR_DYN_DATES = "_dts";
  public static final String SOLR_DYN_DOUBLES = "_ds";
  public static final String SOLR_DYN_FLOATS = "_fs";
  public static final String SOLR_DYN_INTS = "_is";
  public static final String SOLR_DYN_LONGS = "_ls";
  public static final String SOLR_DYN_STRINGS = "_ss";

  // indexed, stored, trie-based
  public static final String SOLR_DYN_CURRENCY = "_c";
  public static final String SOLR_DYN_TDATE = "_tdt";
  public static final String SOLR_DYN_TDOUBLE = "_td";
  public static final String SOLR_DYN_TFLOAT = "_tf";
  public static final String SOLR_DYN_TINT = "_ti";
  public static final String SOLR_DYN_TLONG = "_tl";

  // indexed, stored, multiValued, trie-based
  public static final String SOLR_DYN_TDATES = "_tdts";
  public static final String SOLR_DYN_TDOUBLES = "_tds";
  public static final String SOLR_DYN_TFLOATS = "_tfs";
  public static final String SOLR_DYN_TINTS = "_tis";
  public static final String SOLR_DYN_TLONGS = "_tls";

  /*
   * LOCAL USER DATA
   */
  public static final Path USER_DBVIEWER_DIR = Paths.get(System.getProperty("java.home"), ".db-visualization-toolkit");

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
