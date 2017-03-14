package com.databasepreservation.visualization.shared;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

/**
 * GWT-safe constants used in Database Viewer
 *
 * @see com.databasepreservation.visualization.ViewerConstants for the non
 *      GWT-safe constants
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerSafeConstants {
  /*
   * SOLR CONFIGSETS
   */
  public static final String SOLR_CONFIGSET_DATABASE_RESOURCE = "solr-configset/dbvtk_database";
  public static final String SOLR_CONFIGSET_TABLE_RESOURCE = "solr-configset/dbvtk_table";
  public static final String SOLR_CONFIGSET_SEARCHES_RESOURCE = "solr-configset/dbvtk_searches";
  public static final String SOLR_CONFIGSET_DATABASE = "dbvtk_database";
  public static final String SOLR_CONFIGSET_TABLE = "dbvtk_table";
  public static final String SOLR_CONFIGSET_SEARCHES = "dbvtk_searches";

  /*
   * COLLECTION STRUCTURE
   */
  public static final String SOLR_INDEX_DATABASE_COLLECTION_NAME = "dbv-database";
  public static final String SOLR_INDEX_SEARCHES_COLLECTION_NAME = "dbv-searches";
  public static final String SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX = "dbv-table-";
  public static final String SOLR_INDEX_ROW_COLUMN_NAME_PREFIX = "col";
  public static final String SOLR_INDEX_ROW_LOB_COLUMN_NAME_PREFIX = "lob";

  /*
   * DATABASE FIELDS
   */
  public static final String SOLR_DATABASE_ID = "id";
  public static final String SOLR_DATABASE_STATUS = "status";
  public static final String SOLR_DATABASE_METADATA = "metadata";
  public static final String SOLR_DATABASE_TOTAL_ROWS = "total_rows";
  public static final String SOLR_DATABASE_TOTAL_TABLES = "total_tables";
  public static final String SOLR_DATABASE_TOTAL_SCHEMAS = "total_schemas";
  public static final String SOLR_DATABASE_INGESTED_ROWS = "ingested_rows";
  public static final String SOLR_DATABASE_INGESTED_TABLES = "ingested_tables";
  public static final String SOLR_DATABASE_INGESTED_SCHEMAS = "ingested_schemas";
  public static final String SOLR_DATABASE_CURRENT_TABLE_NAME = "current_table_name";
  public static final String SOLR_DATABASE_CURRENT_SCHEMA_NAME = "current_schema_name";

  /*
   * SEARCHES FIELDS
   */
  public static final String SOLR_SEARCHES_ID = "id";
  public static final String SOLR_SEARCHES_NAME = "name";
  public static final String SOLR_SEARCHES_DESCRIPTION = "description";
  public static final String SOLR_SEARCHES_DATE_ADDED = "date_added";
  public static final String SOLR_SEARCHES_DATABASE_UUID = "database_uuid";
  public static final String SOLR_SEARCHES_TABLE_UUID = "table_uuid";
  public static final String SOLR_SEARCHES_TABLE_NAME = "table_name";
  public static final String SOLR_SEARCHES_SEARCH_INFO_JSON = "search_info_json";

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
  public static final String SOLR_DYN_TDATETIME = "_tdt";
  public static final String SOLR_DYN_TDATE = "_tdt_d";
  public static final String SOLR_DYN_TTIME = "_tdt_t";
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
   * OTHER SOLR
   */
  public static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(SOLR_ROW_SEARCH, "*"));

  /*
   * API related (from RODA)
   */
  public static final String API_SEP = "/";
  public static final String API_REST_V1_AIPS = "api/v1/aips/";
  public static final String API_REST_V1_REPRESENTATIONS = "api/v1/representations/";
  public static final String API_REST_V1_FILES = "api/v1/files/";
  public static final String API_REST_V1_TRANSFERRED = "api/v1/transferred";
  public static final String API_REST_V1_THEME = "api/v1/theme";
  // sub-resources strings
  public static final String API_DATA = "data";
  public static final Object API_FILE = "file";
  public static final String API_DESCRIPTIVE_METADATA = "descriptive_metadata";
  public static final String API_PRESERVATION_METADATA = "preservation_metadata";
  // "http query string" related strings
  public static final String API_QUERY_START = "?";
  public static final String API_QUERY_ASSIGN_SYMBOL = "=";
  public static final String API_QUERY_SEP = "&";
  public static final String API_QUERY_KEY_ACCEPT_FORMAT = "acceptFormat";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_BIN = "bin";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_XML = "xml";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_HTML = "html";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_JSON = "json";
  public static final String API_QUERY_KEY_LANG = "lang";
  public static final String API_QUERY_VALUE_LANG_DEFAULT = ViewerSafeConstants.API_QUERY_VALUE_LANG_PT_PT;
  public static final String API_QUERY_VALUE_LANG_PT_PT = "pt_PT";
  public static final String API_QUERY_VALUE_LANG_EN_US = "en_US";
  public static final String API_QUERY_KEY_START = "start";
  public static final String API_QUERY_KEY_LIMIT = "limit";
  // "http path param" related strings
  public static final String API_PATH_PARAM_AIP_ID = "aip_id";
  public static final String API_PATH_PARAM_REPRESENTATION_ID = "representation_id";
  public static final String API_PATH_PARAM_FILE_UUID = "file_uuid";
  public static final String API_PATH_PARAM_METADATA_ID = "metadata_id";
  public static final String API_QUERY_PARAM_VERSION = "version";
  // http headers used
  public static final String API_HTTP_HEADER_ACCEPT = "Accept";
  // job related params
  public static final String API_PATH_PARAM_JOB_ID = "jobId";
  public static final String API_PATH_PARAM_JOB_JUST_FAILED = "jobJustFailed";
  // api method allowable values
  public static final String API_LIST_MEDIA_TYPES = "json, xml";
  public static final String API_GET_MEDIA_TYPES = "json, xml";
  public static final String API_GET_LIST_MEDIA_TYPES = "json, xml, zip";
  public static final String API_GET_FILE_MEDIA_TYPES = "json, xml, bin";
  public static final String API_POST_PUT_MEDIA_TYPES = "json, xml";
  public static final String API_DELETE_MEDIA_TYPES = "json, xml";
  public static final String API_GET_DESCRIPTIVE_METADATA_MEDIA_TYPES = "json, xml, html, bin";
  public static final String API_DESCRIPTIVE_METADATA_LANGUAGES = "pt_PT, en_US";

  /*
   * API related (for DBVTK)
   */
  public static final String API_SERVLET = "api";
  public static final String API_V1_EXPORT_RESOURCE = "/v1/exports";
  public static final String API_PATH_PARAM_DATABASE_UUID = "databaseUUID";
  public static final String API_PATH_PARAM_TABLE_UUID = "tableUUID";
  public static final String API_QUERY_PARAM_FIELDS = "fl";
  public static final String API_QUERY_PARAM_FILTER = "f";
  public static final String API_QUERY_PARAM_SORTER = "s";
  public static final String API_QUERY_PARAM_SUBLIST = "sl";

  public static final String API_V1_LOBS_RESOURCE = "/v1/lobs";
  public static final String API_PATH_PARAM_ROW_UUID = "rowUUID";
  public static final String API_PATH_PARAM_COLUMN_ID = "columnUUID";

  public static final String API_PATH_PARAM_SOLR_COLLECTION = "collection";
  public static final String API_PATH_PARAM_SOLR_QUERY = "query";

  public static final String API_V1_MANAGE_RESOURCE = "/v1/manage";

  public static final String API_V1_THEME_RESOURCE = "/v1/theme";

  /*
   * Search field types from RODA, plus a few new ones
   */
  public static final String SEARCH_FIELD_TYPE_TEXT = RodaConstants.SEARCH_FIELD_TYPE_TEXT;
  public static final String SEARCH_FIELD_TYPE_DATE_INTERVAL = RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL;
  public static final String SEARCH_FIELD_TYPE_NUMERIC = RodaConstants.SEARCH_FIELD_TYPE_NUMERIC;
  public static final String SEARCH_FIELD_TYPE_NUMERIC_INTERVAL = RodaConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL;
  public static final String SEARCH_FIELD_TYPE_STORAGE = RodaConstants.SEARCH_FIELD_TYPE_STORAGE;
  public static final String SEARCH_FIELD_TYPE_BOOLEAN = RodaConstants.SEARCH_FIELD_TYPE_BOOLEAN;
  public static final String SEARCH_FIELD_TYPE_SUGGEST = RodaConstants.SEARCH_FIELD_TYPE_SUGGEST;
  public static final String SEARCH_FIELD_TYPE_DATETIME = RodaConstants.SEARCH_FIELD_TYPE_DATE;
  public static final String SEARCH_FIELD_TYPE_DATE = RodaConstants.SEARCH_FIELD_TYPE_DATE + "justdate";
  public static final String SEARCH_FIELD_TYPE_TIME = RodaConstants.SEARCH_FIELD_TYPE_DATE + "justtime";

  /*
   * Misc
   */
  public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String SOLRDATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final String SHA1 = "SHA-1";
  public static final String SHA256 = "SHA-256";
  public static final String MD5 = "MD5";
  public static final Long MILLISECONDS_IN_A_DAY = 86400000L;

  /**
   * private constructor
   */
  private ViewerSafeConstants() {
  }
}
