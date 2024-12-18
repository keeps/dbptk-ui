/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client;

import org.roda.core.data.common.RodaConstants;

import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;

/**
 * Constants used in Database Viewer
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {

  // TODO: remove unused, use better names

  public static final String ENDPOINT_MODULES = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_MODULES_RESOURCE;
  public static final String ENDPOINT_SIARD = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_SIARD_RESOURCE;
  public static final String ENDPOINT_CONTEXT = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_CONTEXT_RESOURCE;
  public static final String ENDPOINT_DATABASE = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_DATABASE_RESOURCE;
  public static final String ENDPOINT_EXPORT = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_EXPORT_RESOURCE;
  public static final String ENDPOINT_SEARCH = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_SEARCH_RESOURCE;
  public static final String ENDPOINT_AUTHENTICATION = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_AUTHENTICATION_RESOURCE;
  public static final String ENDPOINT_CLIENT_LOGGER = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_CLIENT_LOGGER_RESOURCE;
  public static final String ENDPOINT_MIGRATION = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_MIGRATION_RESOURCE;
  public static final String ENDPOINT_ACTIVITY_LOG = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_ACTIVITY_LOG_RESOURCE;
  public static final String ENDPOINT_CONFIGURATION = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_CONFIGURATION_RESOURCE;
  public static final String ENDPOINT_JOB = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_JOB_RESOURCE;
  public static final String ENDPOINT_FILE = ViewerConstants.API_SEP + ViewerConstants.API_SERVLET
    + ViewerConstants.API_V1_FILE_RESOURCE;

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
  public static final String LOGGER_DOCKER_METHOD = "CONSOLE";

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
  public static final String VIEWER_ACTIVITY_LOG_FOLDER = "activity-logs";
  public static final String VIEWER_DATABASES_FOLDER = "databases";
  public static final String VIEWER_H2_DATA_FOLDER = "h2/data";

  public static final String MEDIA_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String MEDIA_TYPE_TEXT_HTML = "text/html";

  public static final String MATERIALIZED_VIEW_PREFIX = "VIEW_";
  public static final String CUSTOM_VIEW_PREFIX = "CUSTOM_VIEW_";
  public static final String DATABASE_STATUS_PREFIX = "database-";
  public static final String DENORMALIZATION_STATUS_PREFIX = "denormalization-";

  public static final String INTERNAL_ZIP_LOB_FOLDER = "lobs/";
  public static final String SIARDDK_DEFAULT_SCHEMA_NAME = "public";

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
  public static final String SOLR_INDEX_ACTIVITY_LOGS_COLLECTION_NAME = "dbv-activity-logs";
  public static final String SOLR_INDEX_BATCH_JOBS_COLLECTION_NAME = "dbv-batch-jobs";
  public static final String SOLR_INDEX_ROW_COLUMN_NAME_PREFIX = "col";
  public static final String SOLR_INDEX_ROW_NESTED_COLUMN_NAME_PREFIX = "nest";
  public static final String SOLR_INDEX_ROW_LOB_COLUMN_NAME_PREFIX = "lob";

  /*
   * Solr indexes configuration
   */
  public static final String INDEX_ID = "uuid";
  public static final String INDEX_SEARCH = "search";
  public static final String INDEX_WILDCARD = "*";

  /*
   * COMMON FIELDS
   */
  public static final String SOLR_CONTENT_TYPE = "content_type";

  /*
   * DATABASE FIELDS
   */
  public static final String SOLR_DATABASES_STATUS = "status";
  public static final String SOLR_DATABASES_BROWSE_LOAD_DATE = "browse_loaded_date";
  public static final String SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL = "available_to_search_all";
  public static final String SOLR_DATABASES_METADATA = "metadata";
  public static final String SOLR_DATABASES_SIARD_PATH = "siard_path";
  public static final String SOLR_DATABASES_SIARD_SIZE = "siard_size";
  public static final String SOLR_DATABASES_SIARD_VERSION = "siard_version";
  public static final String SOLR_DATABASES_VALIDATED_AT = "siard_validated_at";
  public static final String SOLR_DATABASES_VALIDATE_VERSION = "siard_validate_version";
  public static final String SOLR_DATABASES_VALIDATOR_REPORT_PATH = "siard_validator_report";
  public static final String SOLR_DATABASES_VALIDATION_STATUS = "siard_validation_status";
  public static final String SOLR_DATABASES_VALIDATION_PASSED = "siard_validation_passed";
  public static final String SOLR_DATABASES_VALIDATION_FAILED = "siard_validation_failed";
  public static final String SOLR_DATABASES_VALIDATION_ERRORS = "siard_validation_errors";
  public static final String SOLR_DATABASES_VALIDATION_WARNINGS = "siard_validation_warnings";
  public static final String SOLR_DATABASES_VALIDATION_SKIPPED = "siard_validation_skipped";
  public static final String SOLR_DATABASES_PERMISSIONS = "database_permissions";
  public static final String SOLR_DATABASES_PERMISSIONS_GROUP = "group_value";
  public static final String SOLR_DATABASES_PERMISSIONS_EXPIRY = "expiry_date";
  public static final String SOLR_DATABASES_CONTENT_TYPE_ROOT = "database";
  public static final String SOLR_DATABASES_CONTENT_TYPE_PERMISSION = "permission";

  /*
   * ACTIVITY LOG FIELDS
   */
  public static final String SOLR_ACTIVITY_LOG_IP_ADDRESS = "ipAddress";
  public static final String SOLR_ACTIVITY_LOG_DATETIME = "datetime";
  public static final String SOLR_ACTIVITY_LOG_USERNAME = "username";
  public static final String SOLR_ACTIVITY_LOG_ACTION_COMPONENT = "actionComponent";
  public static final String SOLR_ACTIVITY_LOG_ACTION_METHOD = "actionMethod";
  public static final String SOLR_ACTIVITY_LOG_RELATED_OBJECT_ID = "relatedObjectId";
  public static final String SOLR_ACTIVITY_LOG_STATE = "state";
  public static final String SOLR_ACTIVITY_LOG_DURATION = "duration";
  public static final String SOLR_ACTIVITY_LOG_LINE_NUMBER = "lineNumber";
  public static final String SOLR_ACTIVITY_LOG_PARAMETERS = "parameters";

  /*
   * BATCH JOB FIELDS
   */
  public static final String SOLR_BATCH_JOB_ID = "jobId";
  public static final String SOLR_BATCH_JOB_DATABASE_UUID = "databaseUUID";
  public static final String SOLR_BATCH_JOB_COLLECTION_UUID = "collectionUUID";
  public static final String SOLR_BATCH_JOB_DATABASE_NAME = "databaseName";
  public static final String SOLR_BATCH_JOB_TABLE_UUID = "tableUUID";
  public static final String SOLR_BATCH_JOB_TABLE_NAME = "tableName";
  public static final String SOLR_BATCH_JOB_SCHEMA_NAME = "schemaName";
  public static final String SOLR_BATCH_JOB_NAME = "name";
  public static final String SOLR_BATCH_JOB_STATUS = "status";
  public static final String SOLR_BATCH_JOB_CREATE_TIME = "createTime";
  public static final String SOLR_BATCH_JOB_START_TIME = "startTime";
  public static final String SOLR_BATCH_JOB_END_TIME = "endTime";
  public static final String SOLR_BATCH_JOB_EXIT_CODE = "exitCode";
  public static final String SOLR_BATCH_JOB_EXIT_DESCRIPTION = "exitCodeDescription";
  public static final String SOLR_BATCH_JOB_ROWS_TO_PROCESS = "rowsToProcess";
  public static final String SOLR_BATCH_JOB_ROWS_PROCESSED = "rowsProcessed";

  public static final String ACTIVITY_LOG_PROPERTY = "activityLogEntry";

  public static final String UI_LISTS_PROPERTY = "ui.lists";
  public static final String UI_LISTS_SEARCH_SELECTEDINFO_LABEL_DEFAULT_I18N_PROPERTY = "search.selectedInfo.label.default.i18n";

  public static final String LISTS_PROPERTY = "lists";

  public static final String LISTS_FACETS_QUERY_PROPERTY = "facets.query";
  public static final String LISTS_FACETS_PARAMETERS_PROPERTY = "facets.parameters";
  public static final String LISTS_FACETS_PARAMETERS_TYPE_PROPERTY = "type";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_PROPERTY = "args";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_START_PROPERTY = "start";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_END_PROPERTY = "end";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_GAP_PROPERTY = "gap";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_LIMIT_PROPERTY = "limit";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_SORT_PROPERTY = "sort";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_VALUES_PROPERTY = "values";
  public static final String LISTS_FACETS_PARAMETERS_ARGS_MINCOUNT_PROPERTY = "minCount";

  public static final String I18N_UI_FACETS_PREFIX = "ui.facets";

  public static final String UI_INTERFACE = "ui.interface";
  public static final String UI_INTERFACE_ROW_PANEL_PROPERTY = "rowPanel";
  public static final String SHOW_NULL_VALUES = "showNullValues";
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
  public static final String SOLR_ROWS_DATABASE_UUID = "databaseUUID";
  public static final String SOLR_ROWS_TABLE_ID = "tableId";
  public static final String SOLR_ROWS_TABLE_UUID = "tableUUID";
  public static final String SOLR_ROWS_NESTED_UUID = "nestedUUID";
  public static final String SOLR_ROWS_NESTED_ORIGINAL_UUID = "nestedOriginalUUID";
  public static final String SOLR_ROWS_NESTED_TABLE_ID = "nestedTableId";
  public static final String SOLR_ROWS_NESTED = "nested";

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
  public static final String SOLR_DYN_TEXT_MULTI = "_txt";
  public static final String SOLR_DYN_NEST_MULTI = "_nst";
  public static final String SOLR_DYN_MIMETYPE = "_mimetype";
  public static final String SOLR_DYN_FILE_EXTENSION = "_fileExtension";

  public static final String SOLR_DYN_LOB_STORE_TYPE = "_store";

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
  public static final String API_PATH_PARAM_COLLECTION_UUID = "collectionUUID";
  public static final String API_PATH_PARAM_EXPORT_DESCRIPTION = "description";
  public static final String API_PATH_PARAM_EXPORT_LOBS = "lobs";
  public static final String API_PATH_PARAM_FILENAME = "filename";
  public static final String API_PATH_PARAM_ZIP_FILENAME = "zfn";
  public static final String API_PATH_PARAM_TABLE_UUID = "tableUUID";
  public static final String API_QUERY_PARAM_FIELDS = "fl";
  public static final String API_QUERY_PARAM_FILTER = "f";
  public static final String API_QUERY_PARAM_EXPORT = "e";
  public static final String API_QUERY_PARAM_SORTER = "s";
  public static final String API_QUERY_PARAM_SUBLIST = "sl";
  public static final String API_QUERY_PARAM_FACETS = "facets";
  public static final String API_QUERY_PARAM_LOCALE = "locale";
  public static final String API_QUERY_PARAM_SEARCH = "search";
  public static final String API_PATH_PARAM_REPORT = "report";
  public static final String API_QUERY_PARAM_RESOURCE_ID = "resource_id";
  public static final String API_QUERY_PARAM_DEFAULT_RESOURCE_ID = "default_resource_id";
  public static final String API_QUERY_PARAM_INLINE = "inline";

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
  public static final String API_PATH_PARAM_DOWNLOAD = "download";
  public static final String API_PATH_PARAM_VALIDATION_REPORT = "validation";

  public static final String API_V1_LOGIN_RESOURCE = "/v1/login";

  public static final String API_V1_MODULES_RESOURCE = "/v1/modules";
  public static final String API_V1_SIARD_RESOURCE = "/v1/siard";
  public static final String API_V1_CONTEXT_RESOURCE = "/v1/context";
  public static final String API_V1_DATABASE_RESOURCE = "/v1/database";
  public static final String API_V1_SEARCH_RESOURCE = "/v1/search";
  public static final String API_V1_AUTHENTICATION_RESOURCE = "/v1/authentication";
  public static final String API_V1_CLIENT_LOGGER_RESOURCE = "/v1/logger";
  public static final String API_V1_MIGRATION_RESOURCE = "/v1/migration";
  public static final String API_V1_ACTIVITY_LOG_RESOURCE = "/v1/activity";
  public static final String API_V1_CONFIGURATION_RESOURCE = "/v1/configuration";
  public static final String API_V1_JOB_RESOURCE = "/v1/job";
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
  public static final String SEARCH_FIELD_TYPE_NESTED = SOLR_ROWS_NESTED;

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
  public static final String APPLICATION_ENV_KEY = "env";
  public static final String APPLICATION_ENV_DESKTOP = "desktop";
  public static final String APPLICATION_ENV_SERVER = "server";

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
  public static final String SIARD_DK = "dk";
  public static final String SIARD_DK_1007 = "dk-1007";
  public static final String SIARD_DK_128 = "dk-128";
  /*
   * DBPTK Metadata
   */
  public static final String SIARD_METADATA_CLIENT_MACHINE = SIARD2ModuleFactory.PARAMETER_META_CLIENT_MACHINE;
  public static final String UPLOAD_WIZARD_MANAGER = "upload-wizard-manager";
  public static final String DOWNLOAD_WIZARD_MANAGER = "download-wizard-manager";

  /*
   * LINKS
   */
  public static final String BLANK_LINK = "_blank";
  public static final String SIARD_SPECIFICATION_LINK = "https://dilcis.eu/content-types/siard/";
  public static final String DBPTK_RELEASE_LINK = "https://github.com/keeps/db-preservation-toolkit/releases/tag/v";
  public static final String ADDITIONAL_CHECKS_SPECIFICATIONLINK = "https://github.com/keeps/db-preservation-toolkit/wiki/Validation#additional-checks";
  public static final String TEMPLATE_ENGINE_LINK = "https://handlebarsjs.com/guide/";

  /**
   * FILE EXTENSIONS
   */
  public static final String CSV_EXTENSION = ".csv";
  public static final String ZIP_EXTENSION = ".zip";
  public static final String JSON_EXTENSION = ".json";

  /*
   * AUTHORIZATION DEFAULT VALUES
   */
  public static final String DEFAULT_ATTRIBUTE_FULLNAME = "fullname";
  public static final String DEFAULT_ATTRIBUTE_EMAIL = "email";
  public static final String DEFAULT_ATTRIBUTE_ROLES = "memberOf";
  public static final String ROLES_PREFIX = "user.attribute.roles.";

  /*
   * ASSISTANT CONTROLLER
   */
  public static final String CONTROLLER_SIARD_PATH_PARAM = "siard-path";
  public static final String CONTROLLER_REPORT_PATH_PARAM = "path";
  public static final String CONTROLLER_DATABASE_ID_PARAM = "databaseUUID";
  public static final String CONTROLLER_COLLECTION_ID_PARAM = "collectionUUID";
  public static final String CONTROLLER_TABLE_ID_PARAM = "tableUUID";
  public static final String CONTROLLER_ROW_ID_PARAM = "rowUUID";
  public static final String CONTROLLER_COLUMN_ID_PARAM = "columnID";
  public static final String CONTROLLER_LOG_ID_PARAM = "logID";
  public static final String CONTROLLER_FILENAME_PARAM = "filename";
  public static final String CONTROLLER_FILTER_PARAM = "filter";
  public static final String CONTROLLER_FACET_PARAM = "facets";
  public static final String CONTROLLER_SUBLIST_PARAM = "sublist";
  public static final String CONTROLLER_SKIP_ADDITIONAL_CHECKS_PARAM = "additional-checks";
  public static final String CONTROLLER_SAVED_SEARCH_NAME_PARAM = "name";
  public static final String CONTROLLER_SAVED_SEARCH_DESCRIPTION_PARAM = "description";
  public static final String CONTROLLER_SAVED_SEARCH_PARAM = "saved-search";
  public static final String CONTROLLER_SAVED_SEARCH_UUID_PARAM = "saved-search-uuid";
  public static final String CONTROLLER_RETRIEVE_COUNT = "total-count";
  public static final String CONTROLLER_USERNAME_PARAM = "username";
  public static final String CONTROLLER_EXPORT_DESCRIPTIONS_PARAM = "exportDescription";
  public static final String CONTROLLER_EXPORT_LOBS_PARAM = "exportLobs";
  public static final String CONTROLLER_ZIP_FILENAME_PARAM = "zipFilename";

  /*
   * REST CONTROLLERS
   */
  public static final String CONTROLLER_ACTIVITY_LOG_RESOURCE = "com.databasepreservation.common.api.v1.ActivityLogResource";
  public static final String CONTROLLER_DATABASE_RESOURCE = "com.databasepreservation.common.api.v1.DatabaseResource";
  public static final String CONTROLLER_COLLECTION_RESOURCE = "com.databasepreservation.common.api.v1.CollectionResource";
  public static final String CONTROLLER_FILE_RESOURCE = "com.databasepreservation.common.api.v1.FileResource";
  public static final String CONTROLLER_JOB_RESOURCE = "com.databasepreservation.common.api.v1.JobResource";
  public static final String CONTROLLER_SIARD_RESOURCE = "com.databasepreservation.common.api.v1.SiardResource";
  public static final String CONTROLLER_USER_LOGIN_CONTROLLER = "com.databasepreservation.common.server.controller.UserLoginController";

  /*
   * Status
   */
  public static final String DATABASE_STATUS_VERSION = "1.0.0";
  public static final String COLLECTION_STATUS_VERSION = "1.0.0";
  public static final String DENORMALIZATION_STATUS_VERSION = "1.0.0";

  /**
   * Template Engine
   */

  public static final String OPEN_TEMPLATE_ENGINE = "{{";
  public static final String CLOSE_TEMPLATE_ENGINE = "}}";
  public static final String TEMPLATE_IIIF_VIEWER_LINK = "iiif_viewer_link";
  public static final String TEMPLATE_LOB_DOWNLOAD_LABEL = "download_label";
  public static final String TEMPLATE_LOB_DOWNLOAD_LINK = "download_link";
  public static final String TEMPLATE_LOB_ROW_INDEX = "row_index";
  public static final String TEMPLATE_LOB_COLUMN_INDEX = "column_index";
  public static final String TEMPLATE_LOB_AUTO_DETECTED_MIME_TYPE = "auto_detected_mime_type";
  public static final String TEMPLATE_LOB_AUTO_DETECTED_EXTENSION = "auto_detected_extension";
  public static final String DEFAULT_VIEWER_DOWNLOAD_LABEL_TEMPLATE = "<a href=\""
    + ViewerConstants.OPEN_TEMPLATE_ENGINE
    + ViewerConstants.TEMPLATE_IIIF_VIEWER_LINK + ViewerConstants.CLOSE_TEMPLATE_ENGINE + ViewerConstants.OPEN_TEMPLATE_ENGINE
    + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK + ViewerConstants.CLOSE_TEMPLATE_ENGINE + "\">"
    + ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL
    + ViewerConstants.CLOSE_TEMPLATE_ENGINE + "</a>";
  public static final String DEFAULT_DOWNLOAD_LABEL_TEMPLATE = "<a href=\"" + ViewerConstants.OPEN_TEMPLATE_ENGINE
    + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK + ViewerConstants.CLOSE_TEMPLATE_ENGINE + "\">"
    + ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL
    + ViewerConstants.CLOSE_TEMPLATE_ENGINE + "</a>";
  public static final String DEFAULT_DETAILED_VIEWER_LABEL_TEMPLATE = "<iframe class=\"embedded-iiif-viewer\" src=\""
    + ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_IIIF_VIEWER_LINK + ViewerConstants.CLOSE_TEMPLATE_ENGINE
    + ViewerConstants.OPEN_TEMPLATE_ENGINE + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK
    + ViewerConstants.CLOSE_TEMPLATE_ENGINE + "\">" + ViewerConstants.OPEN_TEMPLATE_ENGINE
    + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL + ViewerConstants.CLOSE_TEMPLATE_ENGINE + "</iframe>";

  /**
   * SIARD prefixes
   */
  public static final String SIARD_TABLE_PREFIX = "table";
  public static final String SIARD_SCHEMA_PREFIX = "schema";
  public static final String SIARD_LOB_FOLDER_PREFIX = "lob";
  public static final String SIARD_RECORD_PREFIX = "record";
  public static final String SIARD_LOB_OUTSIDE_PREFIX = "external:";
  public static final String SIARD_LOB_INSIDE_PREFIX = "internal:";
  public static final String SIARD_EMBEDDED_LOB_PREFIX = "base64:";
  public static final String SIARD_LOB_FILE_EXTENSION = ".bin";

  public static final String DEFAULT_USERNAME = "admin";
  public static final String DEFAULT_FULL_NAME = "admin";

  public static final String PROPERTY_DISABLE_SIARD_DELETION = "ui.disable.siard.deletion";
  public static final String PROPERTY_DISABLE_AUTO_DETECT_MIME_TYPE = "ui.disable.autoDetect.mimeType";
  public static final String PROPERTY_PLUGIN_LOAD_ON_ACCESS = "ui.plugin.loadOnAccess";

  public static final String ALIAS_PREFIX = "alias-";
  public static final String TEMP_PREFIX = "temp-";

  public static final String EMPTY_SEARCH = "";

  /**
   * Search all
   */
  public static final String LOCAL_STORAGE_SEARCH_ALL_SELECTION = "searchAllSelection";
  public static final String SEARCH_ALL_SELECTED_ALL = "all";
  public static final String SEARCH_ALL_SELECTED_NONE = "none";
  public static final String PROPERTY_SEARCH_ALL_DEFAULT_SELECTION = "ui.searchAll.defaultSelection";

  /**
   * Permissions
   */
  public static final String PROPERTY_EXPIRY_ZONE_ID_OVERRIDE = "permissions.expiry.zoneId.override";

  /**
   * Header
   */
  public static final String DEFAULT_PROPERTY_UI_HEADER_TITLE = "<img src=\"api/v1/theme?resource_id=dbptk_logo_white_vector.svg\" class=\"header-logo\"><span class=\"header-text\">DBPTK Enterprise</span>";
  public static final String PROPERTY_UI_HEADER_TITLE = "ui.header.title";

  /*
   * Show schema name in reference table
   */
  public static final String PROPERTY_REFERENCE_TABLE_SHOW_SCHEMA_NAME = "ui.reference.table.show.schema.name";

  public enum SiardVersion {
    V1_0, V2_0, V2_1, DK, DK_1007, DK_128;
  }

  /**
   * private constructor
   */
  private ViewerConstants() {
  }

  /**
   * External viewer information
   */

  public static final String IIIF_EXTERNAL_VIEWER_SERVICE_NAME = "ui.iiif_viewer.service_name";
  public static final String PRESENTATION_EXTERNAL_SERVICE_NAME = "ui.iiif_viewer.presentation.service_name";
  public static final String VIEWER_ENABLED = "ui.iiif_viewer.enabled";
}
