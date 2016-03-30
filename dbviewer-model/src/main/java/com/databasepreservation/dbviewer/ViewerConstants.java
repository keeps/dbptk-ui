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
  public static final String SOLR_INDEX_TABLE_PREFIX = "tab-";


  /*
   * DATABASE FIELDS
   */
  public static final String SOLR_DATABASE_ID = "id";
  public static final String SOLR_DATABASE_METADATA = "metadata";

  /*
   * TABLE FIELDS
   */
  public static final String SOLR_TABLE_ID = "id";
  public static final String SOLR_TABLE_COLUMN_PREFIX = "col_";

  /*
   * DYNAMIC FIELD TYPES (suffixes)
   */
  //indexed, stored
  public static final String SOLR_DYN_BOOLEAN = "_b";
  public static final String SOLR_DYN_DATE = "_dt";
  public static final String SOLR_DYN_DOUBLE = "_d";
  public static final String SOLR_DYN_FLOAT = "_f";
  public static final String SOLR_DYN_INT = "_i";
  public static final String SOLR_DYN_LOCATION = "_p";
  public static final String SOLR_DYN_LOCATION_RPT = "_srpt";
  public static final String SOLR_DYN_LONG = "_l";
  public static final String SOLR_DYN_STRING = "_s";
  public static final String SOLR_DYN_TEXT_GENERAL = "_txt";

  //indexed, stored, multiValued
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
