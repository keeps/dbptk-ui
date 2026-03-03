/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index.schema.collections;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_CONTENT_TYPE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_BROWSE_LOAD_DATE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PERMISSIONS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PERMISSIONS_EXPIRY;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PERMISSIONS_GROUP;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SIARD_PATH;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SIARD_SIZE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SIARD_VERSION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_STATUS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATED_AT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATE_VERSION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_ERRORS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_FAILED;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_PASSED;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_SKIPPED;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_STATUS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_WARNINGS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATOR_REPORT_PATH;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVER_CONTACT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_DATA_OWNER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_LOB_FOLDER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_PRODUCER_APPLICATION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVAL_DATE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_CLIENT_MACHINE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_DATABASE_PRODUCT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA_DATABASE_USER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_SCHEMAS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_SCHEMA;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SCHEMA_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SCHEMA_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SCHEMA_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SCHEMA_FOLDER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_USERS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_USER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_USER_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_USER_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROLES;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROLE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROLE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROLE_ADMIN;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROLE_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIVILEGES;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIVILEGE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PRIVILEGE_TYPE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PRIVILEGE_GRANTOR;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PRIVILEGE_GRANTEE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PRIVILEGE_OBJECT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PRIVILEGE_OPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PRIVILEGE_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TABLES;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TABLE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_ID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_FOLDER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_ROWS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_SCHEMA_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_SCHEMA_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_NAME_WITHOUT_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_CUSTOM_VIEW;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TABLE_MATERIALIZED_VIEW;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_VIEWS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_VIEW;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_QUERY;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_QUERY_ORIGINAL;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_SCHEMA_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VIEW_SCHEMA_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMNS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_COLUMN;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_SOLR_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_TYPE_ORIGINAL;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_TYPE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_TYPE_DB;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_DEFAULT_VALUE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_NILLABLE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_AUTO_INCREMENT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_COLUMN_INDEX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEYS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEY;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PK_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PK_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PK_COLUMN_INDEXES;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEYS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEY;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_REFERENCED_TABLE_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_REFERENCED_TABLE_ID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_MATCH_TYPE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_DELETE_ACTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_UPDATE_ACTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_REFERENCE_SOURCE_IDX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_FK_REFERENCE_REF_IDX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEYS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEY;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CK_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CK_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CK_COLUMN_INDEXES;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINTS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CHECK_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CHECK_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CHECK_CONDITION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TRIGGERS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_TRIGGER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TRIGGER_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TRIGGER_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TRIGGER_ACTION_TIME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TRIGGER_EVENT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TRIGGER_ALIAS_LIST;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_TRIGGER_ACTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROUTINES;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROUTINE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_SOURCE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_BODY;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_CHARACTERISTIC;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_ROUTINE_RETURN_TYPE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PARAMETERS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PARAMETER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PARAMETER_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PARAMETER_MODE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PARAMETER_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PARAMETER_TYPE_ORIGINAL;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PARAMETER_TYPE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_PARAMETER_TYPE_DB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.structure.ViewerCandidateKey;
import com.databasepreservation.common.client.models.structure.ViewerCheckConstraint;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerPrimaryKey;
import com.databasepreservation.common.client.models.structure.ViewerPrivilegeStructure;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerRoleStructure;
import com.databasepreservation.common.client.models.structure.ViewerRoutine;
import com.databasepreservation.common.client.models.structure.ViewerRoutineParameter;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerTrigger;
import com.databasepreservation.common.client.models.structure.ViewerUserStructure;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.schema.AbstractSolrCollection;
import com.databasepreservation.common.server.index.schema.CopyField;
import com.databasepreservation.common.server.index.schema.Field;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;

public class DatabasesCollection extends AbstractSolrCollection<ViewerDatabase> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabasesCollection.class);

  @Override
  public Class<ViewerDatabase> getObjectClass() {
    return ViewerDatabase.class;
  }

  @Override
  public String getIndexName() {
    return ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(SOLR_DATABASES_STATUS, Field.TYPE_STRING).setIndexed(true).setRequired(true));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_NAME, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_DESCRIPTION, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_ARCHIVER, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_ARCHIVER_CONTACT, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_DATA_OWNER, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_LOB_FOLDER, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_PRODUCER_APPLICATION, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_ARCHIVAL_DATE, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_CLIENT_MACHINE, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_DATABASE_PRODUCT, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_METADATA_DATABASE_USER, Field.TYPE_TEXT));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SCHEMA_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SCHEMA_NAME, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SCHEMA_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SCHEMA_FOLDER, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_USER_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_USER_DESCRIPTION, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROLE_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROLE_ADMIN, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROLE_DESCRIPTION, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PRIVILEGE_TYPE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PRIVILEGE_GRANTOR, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PRIVILEGE_GRANTEE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PRIVILEGE_OBJECT, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PRIVILEGE_OPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PRIVILEGE_DESCRIPTION, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_ID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_NAME, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_FOLDER, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_ROWS, Field.TYPE_LONG));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_SCHEMA_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_SCHEMA_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_NAME_WITHOUT_PREFIX, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_CUSTOM_VIEW, Field.TYPE_BOOLEAN));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TABLE_MATERIALIZED_VIEW, Field.TYPE_BOOLEAN));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_NAME, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_QUERY, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_QUERY_ORIGINAL, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_SCHEMA_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VIEW_SCHEMA_NAME, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_SOLR_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_NAME, Field.TYPE_TEXT));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_TYPE_ORIGINAL, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_TYPE_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_TYPE_DB, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_DEFAULT_VALUE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_NILLABLE, Field.TYPE_BOOLEAN));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_AUTO_INCREMENT, Field.TYPE_BOOLEAN));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_COLUMN_INDEX, Field.TYPE_INT));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PK_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PK_DESCRIPTION, Field.TYPE_STRING));
    fields.add(new Field(SOLR_DATABASES_PK_COLUMN_INDEXES, Field.TYPE_INT).setIndexed(true).setStored(true)
      .setRequired(false).setMultiValued(true));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_REFERENCED_TABLE_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_REFERENCED_TABLE_ID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_MATCH_TYPE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_DELETE_ACTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_FK_UPDATE_ACTION, Field.TYPE_STRING));
    fields.add(new Field(SOLR_DATABASES_FK_REFERENCE_SOURCE_IDX, Field.TYPE_INT).setIndexed(true).setStored(true)
      .setRequired(false).setMultiValued(true));
    fields.add(new Field(SOLR_DATABASES_FK_REFERENCE_REF_IDX, Field.TYPE_INT).setIndexed(true).setStored(true)
      .setRequired(false).setMultiValued(true));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_CK_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_CK_DESCRIPTION, Field.TYPE_STRING));
    fields.add(new Field(SOLR_DATABASES_CK_COLUMN_INDEXES, Field.TYPE_INT).setIndexed(true).setStored(true)
      .setRequired(false).setMultiValued(true));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_CHECK_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_CHECK_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_CHECK_CONDITION, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TRIGGER_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TRIGGER_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TRIGGER_ACTION_TIME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TRIGGER_EVENT, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TRIGGER_ALIAS_LIST, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_TRIGGER_ACTION, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_UUID, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_SOURCE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_BODY, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_CHARACTERISTIC, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_ROUTINE_RETURN_TYPE, Field.TYPE_STRING));

    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PARAMETER_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PARAMETER_MODE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PARAMETER_DESCRIPTION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PARAMETER_TYPE_ORIGINAL, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PARAMETER_TYPE_NAME, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_PARAMETER_TYPE_DB, Field.TYPE_STRING));

    fields.add(new Field(SOLR_DATABASES_VALIDATION_STATUS, Field.TYPE_STRING).setIndexed(true));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SIARD_PATH, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SIARD_SIZE, Field.TYPE_LONG));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SIARD_VERSION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATED_AT, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATE_VERSION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATOR_REPORT_PATH, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_PASSED, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_FAILED, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_ERRORS, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_WARNINGS, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_SKIPPED, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_BROWSE_LOAD_DATE, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL, Field.TYPE_BOOLEAN));
    fields.add(new Field(SOLR_CONTENT_TYPE, Field.TYPE_STRING));
    fields.add(new Field(SOLR_DATABASES_PERMISSIONS_GROUP, Field.TYPE_STRING));
    fields.add(new Field(SOLR_DATABASES_PERMISSIONS_EXPIRY, Field.TYPE_DATE));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_SCHEMAS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_TABLES, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_VIEWS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_COLUMNS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields
      .add(new Field(SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEYS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields
      .add(new Field(SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEYS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields
      .add(new Field(SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEYS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(
      new Field(SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINTS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_TRIGGERS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_ROUTINES, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_PARAMETERS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_USERS, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_ROLES, Field.TYPE_STRING).setIndexed(false).setStored(false));
    fields.add(new Field(SOLR_DATABASES_CONTENT_TYPE_PRIVILEGES, Field.TYPE_STRING).setIndexed(false).setStored(false));

    return fields;
  }

  private Field newIndexedStoredNotRequiredField(String name, String type) {
    return new Field(name, type).setIndexed(true).setStored(true).setRequired(false);
  }

  @Override
  public SolrInputDocument toSolrDocument(ViewerDatabase object) throws ViewerException, RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(object);

    doc.addField(SOLR_DATABASES_STATUS, object.getStatus().toString());

    ViewerMetadata meta = object.getMetadata();
    populateMetadataInDocument(meta, doc, false);

    doc.addField(SOLR_DATABASES_SIARD_PATH, object.getPath());
    doc.addField(SOLR_DATABASES_SIARD_SIZE, object.getSize());
    doc.addField(SOLR_DATABASES_SIARD_VERSION, object.getVersion());
    doc.addField(SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL, object.isAvailableToSearchAll());

    doc.addField(SOLR_DATABASES_VALIDATED_AT, object.getValidatedAt());
    doc.addField(SOLR_DATABASES_VALIDATOR_REPORT_PATH, object.getValidatorReportPath());
    doc.addField(SOLR_DATABASES_VALIDATE_VERSION, object.getValidatedVersion());
    doc.addField(SOLR_DATABASES_VALIDATION_STATUS, object.getValidationStatus().toString());
    doc.addField(SOLR_DATABASES_VALIDATION_PASSED, object.getValidationPassed());
    doc.addField(SOLR_DATABASES_VALIDATION_FAILED, object.getValidationFailed());
    doc.addField(SOLR_DATABASES_VALIDATION_ERRORS, object.getValidationErrors());
    doc.addField(SOLR_DATABASES_VALIDATION_WARNINGS, object.getValidationWarnings());
    doc.addField(SOLR_DATABASES_VALIDATION_SKIPPED, object.getValidationSkipped());

    List<SolrInputDocument> permissionsDocs = new ArrayList<>();
    if (object.getPermissions() != null) {
      for (String groupValue : object.getPermissions().keySet()) {
        SolrInputDocument childPermissionsDoc = new SolrInputDocument();
        AuthorizationDetails authorizationDetails = object.getPermissions().get(groupValue);
        childPermissionsDoc.addField(SOLR_DATABASES_PERMISSIONS_GROUP, groupValue);
        childPermissionsDoc.addField(SOLR_DATABASES_PERMISSIONS_EXPIRY, authorizationDetails.getExpiry());
        childPermissionsDoc.addField(SOLR_CONTENT_TYPE, ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PERMISSION);
        // Dummy field because the schema requires it
        childPermissionsDoc.addField(SOLR_DATABASES_STATUS, SOLR_DATABASES_STATUS);

        permissionsDocs.add(childPermissionsDoc);
      }
    }
    // Added to distinguish parent documents from nested documents
    doc.addField(SOLR_CONTENT_TYPE, ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROOT);
    doc.setField(SOLR_DATABASES_PERMISSIONS, permissionsDocs);

    doc.addField(SOLR_DATABASES_BROWSE_LOAD_DATE, object.getLoadedAt());

    return doc;
  }

  public static void populateMetadataInDocument(ViewerMetadata metadata, SolrInputDocument doc, boolean isUpdate) {
    if (metadata != null) {
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_NAME, metadata.getName(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_DESCRIPTION, metadata.getDescription(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_ARCHIVER, metadata.getArchiver(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_ARCHIVER_CONTACT, metadata.getArchiverContact(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_DATA_OWNER, metadata.getDataOwner(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN, metadata.getDataOriginTimespan(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_PRODUCER_APPLICATION, metadata.getProducerApplication(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_ARCHIVAL_DATE, metadata.getArchivalDate(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_CLIENT_MACHINE, metadata.getClientMachine(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_DATABASE_PRODUCT, metadata.getDatabaseProduct(), isUpdate);
      addOrUpdateField(doc, SOLR_DATABASES_METADATA_DATABASE_USER, metadata.getDatabaseUser(), isUpdate);

      if (metadata.getSchemas() != null) {
        List<SolrInputDocument> schemasDocs = new ArrayList<>();
        for (ViewerSchema schema : metadata.getSchemas()) {
          SolrInputDocument schemaDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_SCHEMA);
          schemaDoc.addField(SOLR_DATABASES_SCHEMA_UUID, schema.getUuid());
          schemaDoc.addField(SOLR_DATABASES_SCHEMA_NAME, schema.getName());
          schemaDoc.addField(SOLR_DATABASES_SCHEMA_DESCRIPTION, schema.getDescription());
          schemaDoc.addField(SOLR_DATABASES_SCHEMA_FOLDER, schema.getFolder());

          if (schema.getTables() != null) {
            List<SolrInputDocument> tablesDocs = new ArrayList<>();
            for (ViewerTable table : schema.getTables()) {
              SolrInputDocument tableDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_TABLE);
              tableDoc.addField(SOLR_DATABASES_TABLE_UUID, table.getUuid());
              tableDoc.addField(SOLR_DATABASES_TABLE_ID, table.getId());
              tableDoc.addField(SOLR_DATABASES_TABLE_NAME, table.getName());
              tableDoc.addField(SOLR_DATABASES_TABLE_DESCRIPTION, table.getDescription());
              tableDoc.addField(SOLR_DATABASES_TABLE_FOLDER, table.getFolder());
              tableDoc.addField(SOLR_DATABASES_TABLE_ROWS, table.getCountRows());
              tableDoc.addField(SOLR_DATABASES_TABLE_SCHEMA_UUID, table.getSchemaUUID());
              tableDoc.addField(SOLR_DATABASES_TABLE_SCHEMA_NAME, table.getSchemaName());
              tableDoc.addField(SOLR_DATABASES_TABLE_NAME_WITHOUT_PREFIX, table.getNameWithoutPrefix());
              tableDoc.addField(SOLR_DATABASES_TABLE_CUSTOM_VIEW, table.isCustomView());
              tableDoc.addField(SOLR_DATABASES_TABLE_MATERIALIZED_VIEW, table.isMaterializedView());

              if (table.getColumns() != null && !table.getColumns().isEmpty()) {
                List<SolrInputDocument> colsDocs = new ArrayList<>();
                for (ViewerColumn column : table.getColumns()) {
                  colsDocs.add(buildColumnDoc(column));
                }
                tableDoc.setField(SOLR_DATABASES_CONTENT_TYPE_COLUMNS, colsDocs);
              }

              if (table.getPrimaryKey() != null) {
                List<SolrInputDocument> pkDocs = new ArrayList<>();
                pkDocs.add(buildPrimaryKeyDoc(table.getPrimaryKey()));
                tableDoc.setField(SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEYS, pkDocs);
              }

              if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                List<SolrInputDocument> fkDocs = new ArrayList<>();
                for (ViewerForeignKey fk : table.getForeignKeys()) {
                  fkDocs.add(buildForeignKeyDoc(fk));
                }
                tableDoc.setField(SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEYS, fkDocs);
              }

              if (table.getCandidateKeys() != null && !table.getCandidateKeys().isEmpty()) {
                List<SolrInputDocument> ckDocs = new ArrayList<>();
                for (ViewerCandidateKey ck : table.getCandidateKeys()) {
                  ckDocs.add(buildCandidateKeyDoc(ck));
                }
                tableDoc.setField(SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEYS, ckDocs);
              }

              if (table.getCheckConstraints() != null && !table.getCheckConstraints().isEmpty()) {
                List<SolrInputDocument> checkDocs = new ArrayList<>();
                for (ViewerCheckConstraint check : table.getCheckConstraints()) {
                  checkDocs.add(buildCheckConstraintDoc(check));
                }
                tableDoc.setField(SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINTS, checkDocs);
              }

              if (table.getTriggers() != null && !table.getTriggers().isEmpty()) {
                List<SolrInputDocument> triggerDocs = new ArrayList<>();
                for (ViewerTrigger trigger : table.getTriggers()) {
                  triggerDocs.add(buildTriggerDoc(trigger));
                }
                tableDoc.setField(SOLR_DATABASES_CONTENT_TYPE_TRIGGERS, triggerDocs);
              }

              tablesDocs.add(tableDoc);
            }
            schemaDoc.setField(SOLR_DATABASES_CONTENT_TYPE_TABLES, tablesDocs);
          }

          if (schema.getViews() != null) {
            List<SolrInputDocument> viewsDocs = new ArrayList<>();
            for (ViewerView view : schema.getViews()) {
              SolrInputDocument viewDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_VIEW);
              viewDoc.addField(SOLR_DATABASES_VIEW_UUID, view.getUuid());
              viewDoc.addField(SOLR_DATABASES_VIEW_NAME, view.getName());
              viewDoc.addField(SOLR_DATABASES_VIEW_DESCRIPTION, view.getDescription());
              viewDoc.addField(SOLR_DATABASES_VIEW_QUERY, view.getQuery());
              viewDoc.addField(SOLR_DATABASES_VIEW_QUERY_ORIGINAL, view.getQueryOriginal());
              viewDoc.addField(SOLR_DATABASES_VIEW_SCHEMA_UUID, view.getSchemaUUID());
              viewDoc.addField(SOLR_DATABASES_VIEW_SCHEMA_NAME, view.getSchemaName());

              if (view.getColumns() != null && !view.getColumns().isEmpty()) {
                List<SolrInputDocument> colsDocs = new ArrayList<>();
                for (ViewerColumn column : view.getColumns()) {
                  colsDocs.add(buildColumnDoc(column));
                }
                viewDoc.setField(SOLR_DATABASES_CONTENT_TYPE_COLUMNS, colsDocs);
              }
              viewsDocs.add(viewDoc);
            }
            schemaDoc.setField(SOLR_DATABASES_CONTENT_TYPE_VIEWS, viewsDocs);
          }

          if (schema.getRoutines() != null) {
            List<SolrInputDocument> routinesDocs = new ArrayList<>();
            for (ViewerRoutine routine : schema.getRoutines()) {
              SolrInputDocument routineDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_ROUTINE);
              routineDoc.addField(SOLR_DATABASES_ROUTINE_UUID, routine.getUuid());
              routineDoc.addField(SOLR_DATABASES_ROUTINE_NAME, routine.getName());
              routineDoc.addField(SOLR_DATABASES_ROUTINE_DESCRIPTION, routine.getDescription());
              routineDoc.addField(SOLR_DATABASES_ROUTINE_SOURCE, routine.getSource());
              routineDoc.addField(SOLR_DATABASES_ROUTINE_BODY, routine.getBody());
              routineDoc.addField(SOLR_DATABASES_ROUTINE_CHARACTERISTIC, routine.getCharacteristic());
              routineDoc.addField(SOLR_DATABASES_ROUTINE_RETURN_TYPE, routine.getReturnType());

              if (routine.getParameters() != null && !routine.getParameters().isEmpty()) {
                List<SolrInputDocument> paramDocs = new ArrayList<>();
                for (ViewerRoutineParameter param : routine.getParameters()) {
                  paramDocs.add(buildParameterDoc(param));
                }
                routineDoc.setField(SOLR_DATABASES_CONTENT_TYPE_PARAMETERS, paramDocs);
              }
              routinesDocs.add(routineDoc);
            }
            schemaDoc.setField(SOLR_DATABASES_CONTENT_TYPE_ROUTINES, routinesDocs);
          }
          schemasDocs.add(schemaDoc);
        }
        addOrUpdateField(doc, SOLR_DATABASES_CONTENT_TYPE_SCHEMAS, schemasDocs, isUpdate);
      }

      if (metadata.getUsers() != null) {
        List<SolrInputDocument> usersDocs = new ArrayList<>();
        for (ViewerUserStructure user : metadata.getUsers()) {
          SolrInputDocument userDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_USER);
          userDoc.addField(SOLR_DATABASES_USER_NAME, user.getName());
          userDoc.addField(SOLR_DATABASES_USER_DESCRIPTION, user.getDescription());
          usersDocs.add(userDoc);
        }
        addOrUpdateField(doc, SOLR_DATABASES_CONTENT_TYPE_USERS, usersDocs, isUpdate);
      }

      if (metadata.getRoles() != null) {
        List<SolrInputDocument> rolesDocs = new ArrayList<>();
        for (ViewerRoleStructure role : metadata.getRoles()) {
          SolrInputDocument roleDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_ROLE);
          roleDoc.addField(SOLR_DATABASES_ROLE_NAME, role.getName());
          roleDoc.addField(SOLR_DATABASES_ROLE_ADMIN, role.getAdmin());
          roleDoc.addField(SOLR_DATABASES_ROLE_DESCRIPTION, role.getDescription());
          rolesDocs.add(roleDoc);
        }
        addOrUpdateField(doc, SOLR_DATABASES_CONTENT_TYPE_ROLES, rolesDocs, isUpdate);
      }

      if (metadata.getPrivileges() != null) {
        List<SolrInputDocument> privilegesDocs = new ArrayList<>();
        for (ViewerPrivilegeStructure priv : metadata.getPrivileges()) {
          SolrInputDocument privDoc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_PRIVILEGE);
          privDoc.addField(SOLR_DATABASES_PRIVILEGE_TYPE, priv.getType());
          privDoc.addField(SOLR_DATABASES_PRIVILEGE_GRANTOR, priv.getGrantor());
          privDoc.addField(SOLR_DATABASES_PRIVILEGE_GRANTEE, priv.getGrantee());
          privDoc.addField(SOLR_DATABASES_PRIVILEGE_OBJECT, priv.getObject());
          privDoc.addField(SOLR_DATABASES_PRIVILEGE_OPTION, priv.getOption());
          privDoc.addField(SOLR_DATABASES_PRIVILEGE_DESCRIPTION, priv.getDescription());
          privilegesDocs.add(privDoc);
        }
        addOrUpdateField(doc, SOLR_DATABASES_CONTENT_TYPE_PRIVILEGES, privilegesDocs, isUpdate);
      }
    }
  }

  private static SolrInputDocument createChildDoc(String contentType) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(SOLR_CONTENT_TYPE, contentType);

    doc.addField(SOLR_DATABASES_STATUS, SOLR_DATABASES_STATUS);
    return doc;
  }

  private static SolrInputDocument buildColumnDoc(ViewerColumn column) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_COLUMN);
    doc.addField(SOLR_DATABASES_COLUMN_SOLR_NAME, column.getSolrName());
    doc.addField(SOLR_DATABASES_COLUMN_NAME, column.getDisplayName());
    doc.addField(SOLR_DATABASES_COLUMN_DESCRIPTION, column.getDescription());
    doc.addField(SOLR_DATABASES_COLUMN_DEFAULT_VALUE, column.getDefaultValue());
    doc.addField(SOLR_DATABASES_COLUMN_NILLABLE, column.getNillable());
    doc.addField(SOLR_DATABASES_COLUMN_AUTO_INCREMENT, column.getAutoIncrement());
    doc.addField(SOLR_DATABASES_COLUMN_INDEX, column.getColumnIndexInEnclosingTable());

    if (column.getType() != null) {
      doc.addField(SOLR_DATABASES_COLUMN_TYPE_ORIGINAL, column.getType().getOriginalTypeName());
      doc.addField(SOLR_DATABASES_COLUMN_TYPE_NAME, column.getType().getTypeName());
      doc.addField(SOLR_DATABASES_COLUMN_TYPE_DB,
        column.getType().getDbType() != null ? column.getType().getDbType().toString() : null);
    }
    return doc;
  }

  private static SolrInputDocument buildPrimaryKeyDoc(ViewerPrimaryKey pk) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_PRIMARY_KEY);
    doc.addField(SOLR_DATABASES_PK_NAME, pk.getName());
    doc.addField(SOLR_DATABASES_PK_DESCRIPTION, pk.getDescription());
    if (pk.getColumnIndexesInViewerTable() != null) {
      doc.addField(SOLR_DATABASES_PK_COLUMN_INDEXES, pk.getColumnIndexesInViewerTable());
    }
    return doc;
  }

  private static SolrInputDocument buildForeignKeyDoc(ViewerForeignKey fk) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_FOREIGN_KEY);
    doc.addField(SOLR_DATABASES_FK_NAME, fk.getName());
    doc.addField(SOLR_DATABASES_FK_DESCRIPTION, fk.getDescription());
    doc.addField(SOLR_DATABASES_FK_REFERENCED_TABLE_UUID, fk.getReferencedTableUUID());
    doc.addField(SOLR_DATABASES_FK_REFERENCED_TABLE_ID, fk.getReferencedTableId());
    doc.addField(SOLR_DATABASES_FK_MATCH_TYPE, fk.getMatchType());
    doc.addField(SOLR_DATABASES_FK_DELETE_ACTION, fk.getDeleteAction());
    doc.addField(SOLR_DATABASES_FK_UPDATE_ACTION, fk.getUpdateAction());

    if (fk.getReferences() != null) {
      for (ViewerReference ref : fk.getReferences()) {
        doc.addField(SOLR_DATABASES_FK_REFERENCE_SOURCE_IDX, ref.getSourceColumnIndex());
        doc.addField(SOLR_DATABASES_FK_REFERENCE_REF_IDX, ref.getReferencedColumnIndex());
      }
    }
    return doc;
  }

  private static SolrInputDocument buildCandidateKeyDoc(ViewerCandidateKey ck) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_CANDIDATE_KEY);
    doc.addField(SOLR_DATABASES_CK_NAME, ck.getName());
    doc.addField(SOLR_DATABASES_CK_DESCRIPTION, ck.getDescription());
    if (ck.getColumnIndexesInViewerTable() != null) {
      doc.addField(SOLR_DATABASES_CK_COLUMN_INDEXES, ck.getColumnIndexesInViewerTable());
    }
    return doc;
  }

  private static SolrInputDocument buildCheckConstraintDoc(ViewerCheckConstraint check) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_CHECK_CONSTRAINT);
    doc.addField(SOLR_DATABASES_CHECK_NAME, check.getName());
    doc.addField(SOLR_DATABASES_CHECK_DESCRIPTION, check.getDescription());
    doc.addField(SOLR_DATABASES_CHECK_CONDITION, check.getCondition());
    return doc;
  }

  private static SolrInputDocument buildTriggerDoc(ViewerTrigger trigger) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_TRIGGER);
    doc.addField(SOLR_DATABASES_TRIGGER_NAME, trigger.getName());
    doc.addField(SOLR_DATABASES_TRIGGER_DESCRIPTION, trigger.getDescription());
    doc.addField(SOLR_DATABASES_TRIGGER_ACTION_TIME, trigger.getActionTime());
    doc.addField(SOLR_DATABASES_TRIGGER_EVENT, trigger.getTriggerEvent());
    doc.addField(SOLR_DATABASES_TRIGGER_ALIAS_LIST, trigger.getAliasList());
    doc.addField(SOLR_DATABASES_TRIGGER_ACTION, trigger.getTriggeredAction());
    return doc;
  }

  private static SolrInputDocument buildParameterDoc(ViewerRoutineParameter param) {
    SolrInputDocument doc = createChildDoc(SOLR_DATABASES_CONTENT_TYPE_PARAMETER);
    doc.addField(SOLR_DATABASES_PARAMETER_NAME, param.getName());
    doc.addField(SOLR_DATABASES_PARAMETER_MODE, param.getMode());
    doc.addField(SOLR_DATABASES_PARAMETER_DESCRIPTION, param.getDescription());
    if (param.getType() != null) {
      doc.addField(SOLR_DATABASES_PARAMETER_TYPE_ORIGINAL, param.getType().getOriginalTypeName());
      doc.addField(SOLR_DATABASES_PARAMETER_TYPE_NAME, param.getType().getTypeName());
      doc.addField(SOLR_DATABASES_PARAMETER_TYPE_DB,
        param.getType().getDbType() != null ? param.getType().getDbType().toString() : null);
    }
    return doc;
  }

  private static void addOrUpdateField(SolrInputDocument doc, String field, Object value, boolean isUpdate) {
    if (value != null || isUpdate) {
      doc.setField(field, isUpdate ? SolrUtils.asValueUpdate(value) : value);
    }
  }

  @Override
  public ViewerDatabase fromSolrDocument(SolrDocument doc) throws ViewerException {
    ViewerDatabase viewerDatabase = super.fromSolrDocument(doc);

    viewerDatabase.setStatus(SolrUtils.objectToEnum(doc.get(SOLR_DATABASES_STATUS), ViewerDatabaseStatus.class,
      ViewerDatabaseStatus.INGESTING));

    ViewerMetadata metadata = new ViewerMetadata();

    String jsonMetadata = SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA), null);

    if (jsonMetadata == null || jsonMetadata.trim().isEmpty()) {
      metadata.setName(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_NAME), null));
      metadata.setDescription(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_DESCRIPTION), null));
      metadata.setArchiver(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_ARCHIVER), null));
      metadata.setArchiverContact(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_ARCHIVER_CONTACT), null));
      metadata.setDataOwner(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_DATA_OWNER), null));
      metadata.setDataOriginTimespan(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN), null));
      metadata
        .setProducerApplication(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_PRODUCER_APPLICATION), null));
      metadata.setArchivalDate(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_ARCHIVAL_DATE), null));
      metadata.setClientMachine(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_CLIENT_MACHINE), null));
      metadata.setDatabaseProduct(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_DATABASE_PRODUCT), null));
      metadata.setDatabaseUser(SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA_DATABASE_USER), null));

      List<ViewerSchema> schemas = new ArrayList<>();
      List<ViewerUserStructure> users = new ArrayList<>();
      List<ViewerRoleStructure> roles = new ArrayList<>();
      List<ViewerPrivilegeStructure> privileges = new ArrayList<>();

      if (doc.containsKey(SOLR_DATABASES_CONTENT_TYPE_SCHEMAS)
        && doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_SCHEMAS) != null) {
        for (Object child : doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_SCHEMAS)) {
          if (child instanceof SolrDocument) {
            schemas.add(SolrUtils.documentToSchema((SolrDocument) child));
          }
        }
      }

      if (doc.containsKey(SOLR_DATABASES_CONTENT_TYPE_USERS)
        && doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_USERS) != null) {
        for (Object child : doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_USERS)) {
          if (child instanceof SolrDocument) {
            users.add(SolrUtils.documentToUser((SolrDocument) child));
          }
        }
      }

      if (doc.containsKey(SOLR_DATABASES_CONTENT_TYPE_ROLES)
        && doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_ROLES) != null) {
        for (Object child : doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_ROLES)) {
          if (child instanceof SolrDocument) {
            roles.add(SolrUtils.documentToRole((SolrDocument) child));
          }
        }
      }

      if (doc.containsKey(SOLR_DATABASES_CONTENT_TYPE_PRIVILEGES)
        && doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_PRIVILEGES) != null) {
        for (Object child : doc.getFieldValues(SOLR_DATABASES_CONTENT_TYPE_PRIVILEGES)) {
          if (child instanceof SolrDocument) {
            privileges.add(SolrUtils.documentToPrivilege((SolrDocument) child));
          }
        }
      }

      metadata.setSchemas(schemas);
      metadata.setUsers(users);
      metadata.setRoles(roles);
      metadata.setPrivileges(privileges);

    } else {
      // deprecated
      metadata = JsonTransformer.getObjectFromJson(jsonMetadata, ViewerMetadata.class);
    }

    viewerDatabase.setMetadata(metadata);

    viewerDatabase.setPath(SolrUtils.objectToString(doc.get(SOLR_DATABASES_SIARD_PATH), ""));
    viewerDatabase.setSize(SolrUtils.objectToLong(doc.get(SOLR_DATABASES_SIARD_SIZE), 0L));
    viewerDatabase.setVersion(SolrUtils.objectToString(doc.get(SOLR_DATABASES_SIARD_VERSION), "2.1"));
    viewerDatabase
      .setAvailableToSearchAll(SolrUtils.objectToBoolean(doc.get(SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL), true));

    viewerDatabase.setValidatedAt(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATED_AT), ""));
    viewerDatabase.setValidatorReportPath(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATOR_REPORT_PATH), ""));
    viewerDatabase.setValidatedVersion(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATE_VERSION), ""));
    viewerDatabase.setValidationStatus(SolrUtils.objectToEnum(doc.get(SOLR_DATABASES_VALIDATION_STATUS),
      ViewerDatabaseValidationStatus.class, ViewerDatabaseValidationStatus.NOT_VALIDATED));
    viewerDatabase.setValidationPassed(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_PASSED), ""));
    viewerDatabase.setValidationFailed(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_FAILED), ""));
    viewerDatabase.setValidationErrors(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_ERRORS), ""));
    viewerDatabase.setValidationWarnings(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_WARNINGS), ""));
    viewerDatabase.setValidationSkipped(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_SKIPPED), ""));
    viewerDatabase.setLoadedAt(SolrUtils.objectToString(doc.get(SOLR_DATABASES_BROWSE_LOAD_DATE), ""));
    viewerDatabase.setPermissions(SolrUtils.objectToDatabasePermissions(doc.get(SOLR_DATABASES_PERMISSIONS)));

    return viewerDatabase;
  }
}