package com.databasepreservation.common.shared;

import com.databasepreservation.model.parameters.Parameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.modules.siard.SIARD2ModuleFactory;

/**
 * Constants used in Database Viewer
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {

  // TODO: remove unused, use better names

  /*
   * DBVTK CONFIG
   */
  public static final String INSTALL_FOLDER_SYSTEM_PROPERTY = "dbvtk.home";
  public static final String INSTALL_FOLDER_ENVIRONMENT_VARIABLE = "DBVTK_HOME";
  public static final String INSTALL_FOLDER_DEFAULT_SUBFOLDER_UNDER_HOME = ".dbvtk";

  public static final String RUNNING_IN_DOCKER_ENVIRONMENT_VARIABLE = "DBVTK_RUNNING_IN_DOCKER";

  // from logback.xml
  public static final String LOGGER_METHOD_PROPERTY = "dbvtk.loggerMethod";
  public static final String LOGGER_DEFAULT_METHOD = "FILEOUT";
  public static final String LOGGER_DOCKER_METHOD = "STDOUT";

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
  public static final String VIEWER_SIARD_FILES_FOLDER = "SIARD-files";
  public static final String VIEWER_MAPDB_FOLDER = "mapdb";
  public static final String VIEWER_VALIDATIONS_REPORTS_FOLDER = "validations-reports";
  public static final String VIEWER_REPORTS_FOLDER = "reports";
  public static final String VIEWER_INDEX_FOLDER = "index";

  public static final String MEDIA_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String MEDIA_TYPE_TEXT_HTML = "text/html";

  public static final String MATERIALIZED_VIEW_PREFIX = "VIEW_";
  public static final String CUSTOM_VIEW_PREFIX = "CUSTOM_VIEW_";

  public static final String INTERNAL_ZIP_LOB_FOLDER = "lobs/";

  /*
   * SOLR CONFIGSETS
   */
  public static final String SOLR_CONFIGSET_DATABASES_RESOURCE = "solr-configset/dbvtk_databases";
  public static final String SOLR_CONFIGSET_DATABASE_RESOURCE = "solr-configset/dbvtk_database";
  public static final String SOLR_CONFIGSET_SEARCHES_RESOURCE = "solr-configset/dbvtk_searches";
  public static final String SOLR_CONFIGSET_DATABASES = "dbvtk_databases";
  public static final String SOLR_CONFIGSET_DATABASE = "dbvtk_database";
  public static final String SOLR_CONFIGSET_SEARCHES = "dbvtk_searches";

  /*
   * COLLECTION STRUCTURE
   */
  public static final String SOLR_INDEX_DATABASES_COLLECTION_NAME = "dbv-databases";
  public static final String SOLR_INDEX_SEARCHES_COLLECTION_NAME = "dbv-searches";
  public static final String SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX = "dbv-database-";
  public static final String SOLR_INDEX_ROW_COLUMN_NAME_PREFIX = "col";
  public static final String SOLR_INDEX_ROW_LOB_COLUMN_NAME_PREFIX = "lob";

  /*
   * Solr indexes configuration
   */
  public static final String INDEX_ID = "uuid";
  public static final String INDEX_SEARCH = "search";
  public static final String INDEX_WILDCARD = "*";

  /*
   * DATABASE FIELDS
   */
  public static final String SOLR_DATABASES_STATUS = "status";
  public static final String SOLR_DATABASES_METADATA = "metadata";
  public static final String SOLR_DATABASES_TOTAL_ROWS = "total_rows";
  public static final String SOLR_DATABASES_TOTAL_TABLES = "total_tables";
  public static final String SOLR_DATABASES_TOTAL_SCHEMAS = "total_schemas";
  public static final String SOLR_DATABASES_INGESTED_ROWS = "ingested_rows";
  public static final String SOLR_DATABASES_INGESTED_TABLES = "ingested_tables";
  public static final String SOLR_DATABASES_INGESTED_SCHEMAS = "ingested_schemas";
  public static final String SOLR_DATABASES_CURRENT_TABLE_NAME = "current_table_name";
  public static final String SOLR_DATABASES_CURRENT_SCHEMA_NAME = "current_schema_name";
  public static final String SOLR_DATABASES_SIARD_PATH = "siard_path";
  public static final String SOLR_DATABASES_SIARD_SIZE = "siard_size";
  public static final String SOLR_DATABASES_SIARD_VERSION = "siard_version";
  public static final String SOLR_DATABASES_VALIDATED_AT = "siard_validated_at";
  public static final String SOLR_DATABASES_VALIDATE_VERSION = "siard_validate_version";
  public static final String SOLR_DATABASES_VALIDATOR_REPORT_PATH = "siard_validator_report";
  public static final String SOLR_DATABASES_VALIDATION_STATUS = "siard_validation_status";
  public static final String SOLR_DATABASES_VALIDATION_PASSED = "siard_validation_passed";
  public static final String SOLR_DATABASES_VALIDATION_OK = "siard_validation_ok";
  public static final String SOLR_DATABASES_VALIDATION_FAILED = "siard_validation_failed";
  public static final String SOLR_DATABASES_VALIDATION_ERRORS = "siard_validation_errors";
  public static final String SOLR_DATABASES_VALIDATION_WARNINGS = "siard_validation_warnings";
  public static final String SOLR_DATABASES_VALIDATION_SKIPPED = "siard_validation_skipped";

  /*
   * SEARCHES FIELDS
   */
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
  public static final String SOLR_ROWS_TABLE_ID = "tableId";
  public static final String SOLR_ROWS_TABLE_UUID = "tableUUID";

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

  // indexed, stored, trie-based (deprecated) TODO remove
  public static final String SOLR_DYN_TDATETIME = SOLR_DYN_DATE;
  public static final String SOLR_DYN_TDATE = SOLR_DYN_DATE;// OK
  public static final String SOLR_DYN_TTIME = SOLR_DYN_DATE;

  /*
   * OTHER SOLR
   */
  public static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(INDEX_SEARCH, "*"));

  /*
   * API related (from RODA)
   */
  public static final String API_SEP = "/";
  // sub-resources strings
  public static final String API_DATA = "data";
  public static final String API_FILE = "file";
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
  public static final String API_QUERY_VALUE_LANG_DEFAULT = ViewerConstants.API_QUERY_VALUE_LANG_PT_PT;
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
  public static final String API_PATH_PARAM_EXPORT_DESCRIPTION = "description";
  public static final String API_PATH_PARAM_EXPORT_LOBS = "lobs";
  public static final String API_PATH_PARAM_FILENAME = "fn";
  public static final String API_PATH_PARAM_ZIP_FILENAME = "zfn";
  public static final String API_PATH_PARAM_TABLE_UUID = "tableUUID";
  public static final String API_QUERY_PARAM_FIELDS = "fl";
  public static final String API_QUERY_PARAM_FILTER = "f";
  public static final String API_QUERY_PARAM_SORTER = "s";
  public static final String API_QUERY_PARAM_SUBLIST = "sl";

  public static final String API_V1_LOBS_RESOURCE = "/v1/lobs";
  public static final String API_PATH_PARAM_ROW_UUID = "rowUUID";
  public static final String API_PATH_PARAM_COLUMN_ID = "columnUUID";
  public static final String API_PATH_PARAM_LOB_FILENAME = "lobfilename";

  public static final String API_PATH_PARAM_SOLR_COLLECTION = "collection";
  public static final String API_PATH_PARAM_SOLR_QUERY = "query";

  public static final String API_V1_MANAGE_RESOURCE = "/v1/manage";

  public static final String API_V1_THEME_RESOURCE = "/v1/theme";

  public static final String API_V1_REPORT_RESOURCE = "/v1/report";

  public static final String API_V1_FILE_RESOURCE = "/v1/file";
  public static final String API_PATH_PARAM_SIARD = "siard";
  public static final String API_PATH_PARAM_VALIDATION_REPORT = "validation";

  public static final String API_V1_LOGIN_RESOURCE = "/v1/login";

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
  public static final String SIARD_FILES = "SIARD files";
  public static final String REPORT_FILES = "Report files";
  public static final String UDT_FILES = "UDT files";
  public static final String SIARD = "siard";

  /*
   * Application Environment
   */
  public static final String DESKTOP = "desktop";
  public static final String SERVER = "server";

  /*
   * DBPTK Parameters
   */
  public static final String SIARD_EXPORT_OPTIONS = "SIARD_EXPORT_OPTIONS";
  public static final String METADATA_EXPORT_OPTIONS = "METADATA_EXPORT_OPTIONS";
  public static final String EXTERNAL_LOBS_EXPORT_OPTIONS = "EXTERNAL_LOBS";
  public static final String EXPORT_FORMAT_SIARD = "SIARD";
  public static final String EXPORT_FORMAT_DBMS = "DBMS";

  /*
   * SUFFIX
   */
  public static final String TXT_SUFFIX = ".txt";
  public static final String YAML_SUFFIX = ".yaml";
  public static final String SIARD_SUFFIX = ".siard";

  /*
   * DBPTK Input Types
   */
  public static final String INPUT_TYPE_PASSWORD = "PASSWORD";
  public static final String INPUT_TYPE_CHECKBOX = "CHECKBOX";
  public static final String INPUT_TYPE_FILE_OPEN = "FILE_OPEN";
  public static final String INPUT_TYPE_FILE_SAVE = "FILE_SAVE";
  public static final String INPUT_TYPE_FOLDER = "FOLDER";
  public static final String INPUT_TYPE_TEXT = "TEXT";
  public static final String INPUT_TYPE_NUMBER = "NUMBER";
  public static final String INPUT_TYPE_DEFAULT = "DEFAULT";
  public static final String INPUT_TYPE_NONE = "NONE";
  public static final String INPUT_TYPE_COMBOBOX = "COMBOBOX";
  public static final String INPUT_TYPE_DRIVER = "DRIVER";

  /*
   * DBPTK SIARD formats
   */
  public static final String SIARD1 = "siard-1";
  public static final String SIARD2 = "siard-2";
  public static final String SIARDDK = "siard-dk";
  public static final String SIARD2_1 = "siard-2.1";
  public static final String SIARD_V10 = "1.0";
  public static final String SIARD_V20 = "2.0";
  public static final String SIARD_V21 = "2.1";

  /*
   * DBPTK Metadata
   */
  public static final String SIARD_METADATA_CLIENT_MACHINE = SIARD2ModuleFactory.PARAMETER_META_CLIENT_MACHINE;
  public static final String UPLOAD_WIZARD_MANAGER = "upload-wizard-manager";
  public static final String DOWNLOAD_WIZARD_MANAGER = "download-wizard-manager";

  /*
   * LINKS
   */
  public static final String APPLICATION_LINK = "https://visualization.database-preservation.com/";
  public static final String OWNER_LINK = "https://www.keep.pt";
  public static final String NAE_FINANCIER_LINK = "http://www.ra.ee/";
  public static final String DGLAB_FINANCIER_LINK = "http://dglab.gov.pt/";
  public static final String EARK_FINANCIER_LINK = "https://www.eark-project.com/";
  public static final String BLANK_LINK = "_blank";
  public static final String SIARD_SPECIFICATION_LINK = "https://dilcis.eu/content-types/siard/";
  public static final String ADDITIONAL_CHECKS_SPECIFICATIONLINK = "https://github.com/keeps/db-preservation-toolkit/wiki/Validation#additional-checks";

  /**
   * FILE EXTENSIONS
   */
  public static final String CSV_EXTENSION = ".csv";
  public static final String ZIP_EXTENSION = ".zip";

  /**
   * private constructor
   */
  private ViewerConstants() {
  }
}
