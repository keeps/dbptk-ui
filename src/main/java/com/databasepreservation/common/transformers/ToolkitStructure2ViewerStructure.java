/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.transformers;

import com.databasepreservation.common.client.models.structure.ViewerLobStoreType;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCandidateKey;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerCheckConstraint;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseFromToolkit;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerMimeType;
import com.databasepreservation.common.client.models.structure.ViewerPrimaryKey;
import com.databasepreservation.common.client.models.structure.ViewerPrivilegeStructure;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerRoleStructure;
import com.databasepreservation.common.client.models.structure.ViewerRoutine;
import com.databasepreservation.common.client.models.structure.ViewerRoutineParameter;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerTrigger;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.models.structure.ViewerTypeArray;
import com.databasepreservation.common.client.models.structure.ViewerTypeStructure;
import com.databasepreservation.common.client.models.structure.ViewerUserStructure;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.io.providers.PathInputStreamProvider;
import com.databasepreservation.common.io.providers.TemporaryPathInputStreamProvider;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.LobManagerUtils;
import com.databasepreservation.common.utils.ViewerUtils;
import com.databasepreservation.model.data.BinaryCell;
import com.databasepreservation.model.data.Cell;
import com.databasepreservation.model.data.ComposedCell;
import com.databasepreservation.model.data.NullCell;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.data.SimpleCell;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.structure.CandidateKey;
import com.databasepreservation.model.structure.CheckConstraint;
import com.databasepreservation.model.structure.ColumnStructure;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.ForeignKey;
import com.databasepreservation.model.structure.Parameter;
import com.databasepreservation.model.structure.PrimaryKey;
import com.databasepreservation.model.structure.PrivilegeStructure;
import com.databasepreservation.model.structure.Reference;
import com.databasepreservation.model.structure.RoleStructure;
import com.databasepreservation.model.structure.RoutineStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.model.structure.Trigger;
import com.databasepreservation.model.structure.UserStructure;
import com.databasepreservation.model.structure.ViewStructure;
import com.databasepreservation.model.structure.type.ComposedTypeArray;
import com.databasepreservation.model.structure.type.ComposedTypeStructure;
import com.databasepreservation.model.structure.type.SimpleTypeBinary;
import com.databasepreservation.model.structure.type.SimpleTypeBoolean;
import com.databasepreservation.model.structure.type.SimpleTypeDateTime;
import com.databasepreservation.model.structure.type.SimpleTypeEnumeration;
import com.databasepreservation.model.structure.type.SimpleTypeInterval;
import com.databasepreservation.model.structure.type.SimpleTypeNumericApproximate;
import com.databasepreservation.model.structure.type.SimpleTypeNumericExact;
import com.databasepreservation.model.structure.type.SimpleTypeString;
import com.databasepreservation.model.structure.type.Type;
import com.databasepreservation.utils.JodaUtils;
import com.databasepreservation.utils.XMLUtils;

/**
 * Utility class used to convert a DatabaseStructure (used in Database
 * Preservation Toolkit) to a ViewerStructure (used in Database Viewer)
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ToolkitStructure2ViewerStructure {
  private static final Logger LOGGER = LoggerFactory.getLogger(ToolkitStructure2ViewerStructure.class);
  private static boolean simpleMetadata = false;
  private static final Pattern rowIndexPattern = Pattern.compile("^(.*\\.)?(\\d+)$");

  private static final Tika tika = new Tika();
  private static String currentTable;

  /**
   * Private empty constructor
   */
  private ToolkitStructure2ViewerStructure() {
  }

  /**
   * Deep-convert a DatabaseStructure to a ViewerDatabase
   *
   * @param structure
   *          the database structure used by Database Preservation Toolkit
   * @return an equivalent database that can be used by Database Viewer
   */
  public static ViewerDatabaseFromToolkit getDatabase(DatabaseStructure structure) throws ViewerException {
    return getDatabase(structure, SolrUtils.randomUUID(), false);
  }

  /**
   * Deep-convert a DatabaseStructure to a ViewerDatabase
   *
   * @param structure
   *          the database structure used by Database Preservation Toolkit
   * @return an equivalent database that can be used by Database Viewer
   */
  public static ViewerDatabaseFromToolkit getDatabase(DatabaseStructure structure, boolean simpleMetadata)
    throws ViewerException {
    return getDatabase(structure, SolrUtils.randomUUID(), simpleMetadata);
  }

  public static ViewerDatabaseFromToolkit getDatabase(DatabaseStructure structure, String databaseUUID, boolean value)
    throws ViewerException {
    simpleMetadata = value;
    ViewerDatabaseFromToolkit result = new ViewerDatabaseFromToolkit();
    result.setUuid(databaseUUID);
    result.setStatus(ViewerDatabaseStatus.INGESTING);
    result.setMetadata(getMetadata(result, structure));
    return result;
  }

  private static ViewerMetadata getMetadata(ViewerDatabaseFromToolkit vdb, DatabaseStructure structure)
    throws ViewerException {
    ViewerMetadata result = new ViewerMetadata();
    result.setName(structure.getName());
    if (!simpleMetadata) {
      result.setArchiver(structure.getArchiver());
      result.setArchiverContact(structure.getArchiverContact());
      result.setArchivalDate(getArchivalDate(structure));
      result.setClientMachine(structure.getClientMachine());
      result.setDatabaseProduct(getDatabaseProduct(structure));
      result.setDatabaseUser(structure.getDatabaseUser());
      result.setDataOriginTimespan(structure.getDataOriginTimespan());
      result.setDataOwner(structure.getDataOwner());
      result.setProducerApplication(structure.getProducerApplication());

      result.setUsers(getUsers(structure.getUsers()));
      result.setRoles(getRoles(structure.getRoles()));
      result.setPrivileges(getPrivileges(structure.getPrivileges()));
    }

    result.setDescription(structure.getDescription());
    ReferenceHolder references = new ReferenceHolder(structure);

    result.setSchemas(getSchemas(vdb, structure.getSchemas(), references));
    return result;
  }

  public static void mergeMetadata(DatabaseStructure updatedDatabaseStructure, ViewerMetadata metadata) {
    for (SchemaStructure schema : updatedDatabaseStructure.getSchemas()) {
      for (TableStructure table : schema.getTables()) {
        metadata.getTableById(table.getId()).setDescription(table.getDescription());

        if (metadata.getTableById(table.getId()).getPrimaryKey() != null) {
          metadata.getTableById(table.getId()).getPrimaryKey().setDescription(table.getPrimaryKey().getDescription());
        }

        for (ViewerForeignKey foreignKey : metadata.getTableById(table.getId()).getForeignKeys()) {
          foreignKey.setDescription(table.getForeignKeyByName(foreignKey.getName()).getDescription());
        }

        for (ViewerCandidateKey candidateKey : metadata.getTableById(table.getId()).getCandidateKeys()) {
          candidateKey.setDescription(table.getCandidateKeyByName(candidateKey.getName()).getDescription());
        }

        for (ViewerCheckConstraint checkConstraint : metadata.getTableById(table.getId()).getCheckConstraints()) {
          checkConstraint.setDescription(table.getCheckConstraintByName(checkConstraint.getName()).getDescription());
        }

        for (ViewerTrigger trigger : metadata.getTableById(table.getId()).getTriggers()) {
          trigger.setDescription(table.getTriggerByName(trigger.getName()).getDescription());
        }

        int index = 0;
        for (ColumnStructure column : table.getColumns()) {
          metadata.getTableById(table.getId()).getColumnByIndexInEnclosingTable(index++)
            .setDescription(column.getDescription());
        }
      }

      for (ViewStructure view : schema.getViews()) {
        ViewerSchema viewerSchema = metadata.getSchemaByName(schema.getName());

        for (ViewerView viewerView : viewerSchema.getViews()) {
          if (view.getName().equals(viewerView.getName())) {
            metadata.getView(viewerView.getUuid()).setDescription(view.getDescription());

            int index = 0;
            for (ColumnStructure column : view.getColumns()) {
              metadata.getView(viewerView.getUuid()).getColumnByIndexInEnclosing(index++)
                .setDescription(column.getDescription());
            }
          }
        }
      }

      for (RoutineStructure routine : schema.getRoutines()) {
        for (ViewerRoutine viewerRoutine : metadata.getSchemaByName(schema.getName()).getRoutines()) {
          if (viewerRoutine.getName().equals(routine.getName())) {
            viewerRoutine.setDescription(routine.getDescription());

            for (ViewerRoutineParameter parameter : viewerRoutine.getParameters()) {
              parameter.setDescription(routine.getParameterByName(parameter.getName()).getDescription());
            }
          }
        }
      }

      for (UserStructure user : updatedDatabaseStructure.getUsers()) {
        for (ViewerUserStructure metadataUser : metadata.getUsers()) {
          if (metadataUser.getName().equals(user.getName())) {
            metadataUser.setDescription(user.getDescription());
          }
        }
      }
    }

  }

  private static List<ViewerPrivilegeStructure> getPrivileges(List<PrivilegeStructure> privileges) {
    ArrayList<ViewerPrivilegeStructure> result = new ArrayList<>();
    if (privileges != null) {
      for (PrivilegeStructure privilege : privileges) {
        result.add(getPrivilege(privilege));
      }
    }
    return result;
  }

  private static ViewerPrivilegeStructure getPrivilege(PrivilegeStructure privilege) {
    ViewerPrivilegeStructure result = new ViewerPrivilegeStructure();
    result.setType(privilege.getType());
    result.setDescription(privilege.getDescription());
    result.setGrantee(privilege.getGrantee());
    result.setGrantor(privilege.getGrantor());
    result.setObject(privilege.getObject());
    result.setOption(privilege.getOption());
    return result;
  }

  private static List<ViewerRoleStructure> getRoles(List<RoleStructure> roles) {
    ArrayList<ViewerRoleStructure> result = new ArrayList<>();
    if (roles != null) {
      for (RoleStructure role : roles) {
        result.add(getRole(role));
      }
    }
    return result;
  }

  private static ViewerRoleStructure getRole(RoleStructure role) {
    ViewerRoleStructure result = new ViewerRoleStructure();
    result.setName(role.getName());
    result.setAdmin(role.getAdmin());
    result.setDescription(role.getDescription());
    return result;
  }

  private static List<ViewerUserStructure> getUsers(List<UserStructure> users) {
    ArrayList<ViewerUserStructure> result = new ArrayList<>();
    if (users != null) {
      for (UserStructure user : users) {
        result.add(getUser(user));
      }
    }
    return result;
  }

  private static ViewerUserStructure getUser(UserStructure user) {
    ViewerUserStructure result = new ViewerUserStructure();
    result.setName(user.getName());
    result.setDescription(user.getDescription());
    return result;
  }

  private static String getDatabaseProduct(DatabaseStructure structure) {
    if (StringUtils.isNotBlank(structure.getProductVersion())) {
      return structure.getProductName() + " v" + structure.getProductVersion();
    } else {
      return structure.getProductName();
    }
  }

  private static String getArchivalDate(DatabaseStructure structure) {
    return ViewerUtils.dateToString(structure.getArchivalDate().withZone(DateTimeZone.UTC).toDate());
  }

  private static List<ViewerSchema> getSchemas(ViewerDatabaseFromToolkit vdb, List<SchemaStructure> schemas,
    ReferenceHolder references) throws ViewerException {
    List<ViewerSchema> result = new ArrayList<>();
    for (SchemaStructure schema : schemas) {
      result.add(getSchema(vdb, schema, references));
    }
    return result;
  }

  private static ViewerSchema getSchema(ViewerDatabaseFromToolkit vdb, SchemaStructure schema,
    ReferenceHolder references) throws ViewerException {
    ViewerSchema result = new ViewerSchema();
    result.setUuid(SolrUtils.randomUUID());
    result.setName(schema.getName());
    result.setDescription(schema.getDescription());
    result.setFolder(schema.getFolder());
    if (!simpleMetadata) {
      result.setRoutines(getRoutines(vdb, schema.getRoutines()));
    } else {
      result.setRoutines(new ArrayList<>());
    }
    result.setViews(getViews(vdb, schema.getViews()));

    vdb.putSchema(schema.getName(), result);
    result.setTables(getTables(vdb, schema.getTables(), references));

    return result;
  }

  private static List<ViewerView> getViews(ViewerDatabaseFromToolkit vdb, List<ViewStructure> views) {
    ArrayList<ViewerView> result = new ArrayList<>();
    if (views != null) {
      for (ViewStructure view : views) {
        result.add(getView(vdb, view));
      }
    }
    return result;
  }

  private static ViewerView getView(ViewerDatabaseFromToolkit vdb, ViewStructure view) {
    ViewerView result = new ViewerView();
    result.setName(view.getName());
    result.setUuid(SolrUtils.randomUUID());
    try {
      result.setColumns(getColumns(view.getColumns()));
    } catch (ViewerException e) {
      LOGGER.error("Could not convert the columns for view {}", view, e);
      result.setColumns(new ArrayList<>());
    }

    result.setQuery(view.getQuery());
    result.setQueryOriginal(view.getQueryOriginal());
    result.setDescription(view.getDescription());
    vdb.putView(view.getName(), result);

    return result;
  }

  private static List<ViewerRoutine> getRoutines(ViewerDatabaseFromToolkit vdb, List<RoutineStructure> routines) {
    ArrayList<ViewerRoutine> result = new ArrayList<>();
    for (RoutineStructure routine : routines) {
      result.add(getRoutine(vdb, routine));
    }
    return result;
  }

  private static ViewerRoutine getRoutine(ViewerDatabaseFromToolkit vdb, RoutineStructure routine) {
    ViewerRoutine result = new ViewerRoutine();
    result.setName(routine.getName());
    result.setUuid(SolrUtils.randomUUID());

    result.setDescription(routine.getDescription());
    result.setSource(routine.getSource());
    result.setBody(routine.getBody());
    result.setCharacteristic(routine.getCharacteristic());
    result.setReturnType(routine.getReturnType());

    result.setParameters(getRoutineParameters(routine.getParameters()));
    vdb.putRoutine(routine.getName(), result);

    return result;
  }

  private static List<ViewerRoutineParameter> getRoutineParameters(List<Parameter> parameters) {
    ArrayList<ViewerRoutineParameter> result = new ArrayList<>();
    if (parameters != null) {
      for (Parameter parameter : parameters) {
        result.add(getRoutineParameter(parameter));
      }
    }
    return result;
  }

  private static ViewerRoutineParameter getRoutineParameter(Parameter parameter) {
    ViewerRoutineParameter result = new ViewerRoutineParameter();
    result.setName(parameter.getName());
    result.setMode(parameter.getMode());
    result.setDescription(parameter.getDescription());

    try {
      result.setType(getType(parameter.getType()));
    } catch (ViewerException e) {
      LOGGER.debug("Could not convert routine parameter type", e);
    }

    return result;
  }

  private static List<ViewerTable> getTables(ViewerDatabaseFromToolkit vdb, List<TableStructure> tables,
    ReferenceHolder references) throws ViewerException {
    List<ViewerTable> result = new ArrayList<>();
    for (TableStructure table : tables) {
      result.add(getTable(vdb, table, references));
    }
    return result;
  }

  private static ViewerTable getTable(ViewerDatabaseFromToolkit vdb, TableStructure table, ReferenceHolder references)
    throws ViewerException {
    ViewerTable result = new ViewerTable();
    result.setId(table.getId());
    result.setUuid(references.getTableUUID(table.getId()));
    result.setName(table.getName());
    result.setDescription(table.getDescription());
    result.setFolder(table.getFolder());
    result.setCountRows(table.getRows());
    result.setSchemaName(table.getSchema());
    result.setSchemaUUID(vdb.getSchema(result.getSchemaName()).getUuid());
    result.setColumns(getColumns(table.getColumns()));
    if (!simpleMetadata) {
      result.setTriggers(getTriggers(table.getTriggers()));
      result.setPrimaryKey(getPrimaryKey(table, references));
      result.setCandidateKeys(getCandidateKeys(table, references));
      result.setCheckConstraints(getCheckConstraints(table.getCheckConstraints()));
    }
    result.setForeignKeys(getForeignKeys(table, references));
    if (table.getName().startsWith(ViewerConstants.CUSTOM_VIEW_PREFIX)) {
      result.setCustomView(true);
      result.setMaterializedView(false);
      result.setNameWithoutPrefix(table.getName().replaceFirst(ViewerConstants.CUSTOM_VIEW_PREFIX, ""));
    } else if (table.getName().startsWith(ViewerConstants.MATERIALIZED_VIEW_PREFIX)) {
      result.setCustomView(false);
      result.setMaterializedView(true);
      result.setNameWithoutPrefix(table.getName().replaceFirst(ViewerConstants.MATERIALIZED_VIEW_PREFIX, ""));
    } else {
      result.setCustomView(false);
      result.setMaterializedView(false);
      result.setNameWithoutPrefix(table.getName());
    }

    vdb.putTable(table.getId(), result);
    return result;
  }

  private static List<ViewerCheckConstraint> getCheckConstraints(List<CheckConstraint> constraints) {
    ArrayList<ViewerCheckConstraint> result = new ArrayList<>();
    if (constraints != null) {
      for (CheckConstraint constraint : constraints) {
        result.add(getCheckConstraint(constraint));
      }
    }
    return result;
  }

  private static ViewerCheckConstraint getCheckConstraint(CheckConstraint constraint) {
    ViewerCheckConstraint result = new ViewerCheckConstraint();
    result.setName(constraint.getName());
    result.setCondition(constraint.getCondition());
    result.setDescription(constraint.getDescription());
    return result;
  }

  private static List<ViewerCandidateKey> getCandidateKeys(TableStructure table, ReferenceHolder references) {
    List<ViewerCandidateKey> result = new ArrayList<>();
    for (CandidateKey candidateKey : table.getCandidateKeys()) {
      result.add(getCandidateKey(candidateKey, table, references));
    }
    return result;
  }

  private static ViewerCandidateKey getCandidateKey(CandidateKey candidateKey, TableStructure table,
    ReferenceHolder references) {
    ViewerCandidateKey result = new ViewerCandidateKey();

    result.setName(candidateKey.getName());
    result.setDescription(candidateKey.getDescription());

    List<Integer> columnIndexesInTable = new ArrayList<>();
    for (String columnName : candidateKey.getColumns()) {
      columnIndexesInTable.add(references.getIndexForColumn(table.getId(), columnName));
    }

    result.setColumnIndexesInViewerTable(columnIndexesInTable);

    return result;
  }

  private static List<ViewerForeignKey> getForeignKeys(TableStructure table, ReferenceHolder references) {
    List<ViewerForeignKey> result = new ArrayList<>();
    for (ForeignKey foreignKey : table.getForeignKeys()) {
      result.add(getForeignKey(foreignKey, table, references));
    }
    return result;
  }

  private static ViewerForeignKey getForeignKey(ForeignKey foreignKey, TableStructure table,
    ReferenceHolder referenceHolder) {
    ViewerForeignKey result = new ViewerForeignKey();

    result.setName(foreignKey.getName());
    result.setDescription(foreignKey.getDescription());
    result.setDeleteAction(foreignKey.getDeleteAction());
    result.setUpdateAction(foreignKey.getUpdateAction());
    result.setMatchType(foreignKey.getMatchType());

    result.setReferencedTableUUID(
      referenceHolder.getTableUUID(foreignKey.getReferencedSchema(), foreignKey.getReferencedTable()));

    result.setReferencedTableId(
      referenceHolder.getIdFromNames(foreignKey.getReferencedSchema(), foreignKey.getReferencedTable()));

    List<ViewerReference> resultReferences = new ArrayList<>();
    for (Reference reference : foreignKey.getReferences()) {
      ViewerReference resultReference = new ViewerReference();

      resultReference.setSourceColumnIndex(referenceHolder.getIndexForColumn(table.getId(), reference.getColumn()));
      resultReference.setReferencedColumnIndex(referenceHolder.getIndexForColumn(foreignKey.getReferencedSchema(),
        foreignKey.getReferencedTable(), reference.getReferenced()));

      resultReferences.add(resultReference);
    }
    result.setReferences(resultReferences);

    return result;
  }

  private static ViewerPrimaryKey getPrimaryKey(TableStructure table, ReferenceHolder references) {
    PrimaryKey pk = table.getPrimaryKey();
    if (pk != null) {
      ViewerPrimaryKey result = new ViewerPrimaryKey();
      result.setName(pk.getName());
      result.setDescription(pk.getDescription());

      List<Integer> columnIndexesInTable = new ArrayList<>();
      for (String columnName : pk.getColumnNames()) {
        columnIndexesInTable.add(references.getIndexForColumn(table.getId(), columnName));
      }
      result.setColumnIndexesInViewerTable(columnIndexesInTable);
      return result;
    } else {
      return null;
    }
  }

  private static List<ViewerTrigger> getTriggers(List<Trigger> triggers) {
    List<ViewerTrigger> result = new ArrayList<>();
    for (Trigger trigger : triggers) {
      result.add(getTrigger(trigger));
    }
    return result;
  }

  private static ViewerTrigger getTrigger(Trigger trigger) {
    ViewerTrigger result = new ViewerTrigger();
    result.setActionTime(trigger.getActionTime());
    result.setAliasList(trigger.getAliasList());
    result.setDescription(trigger.getDescription());
    result.setName(trigger.getName());
    result.setTriggeredAction(XMLUtils.decode(trigger.getTriggeredAction()));
    result.setTriggerEvent(trigger.getTriggerEvent());
    return result;
  }

  private static List<ViewerColumn> getColumns(List<ColumnStructure> columns) throws ViewerException {
    List<ViewerColumn> result = new ArrayList<>();
    int index = 0;
    for (ColumnStructure column : columns) {
      result.add(getColumn(column, index++));
    }
    return result;
  }

  private static ViewerColumn getColumn(ColumnStructure column, int index) throws ViewerException {
    ViewerColumn result = new ViewerColumn();
    Type columnType = column.getType();

    ViewerType columnViewerType = getType(columnType);
    result.setType(columnViewerType);
    if (!simpleMetadata) {
      result.setSolrName(getColumnSolrName(index, columnType, columnViewerType));
      result.setColumnIndexInEnclosingTable(index);
    }
    result.setDisplayName(column.getName());
    result.setDescription(column.getDescription());
    result.setAutoIncrement(column.getIsAutoIncrement());
    result.setDefaultValue(column.getDefaultValue());
    result.setNillable(column.isNillable());

    return result;
  }

  /**
   * Gets a column name for a type, including the dynamic type suffix
   *
   * @param index
   *          zero based index
   * @param type
   *          the type from database preservation toolkit
   * @return the column name
   * @throws ViewerException
   */
  private static String getColumnSolrName(int index, Type type, ViewerType viewerType) throws ViewerException {
    // suffix must always be set before being used
    String suffix;
    String prefix = ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX;

    if (type instanceof SimpleTypeBinary) {
      suffix = ViewerConstants.SOLR_DYN_STRING;
      prefix = ViewerConstants.SOLR_INDEX_ROW_LOB_COLUMN_NAME_PREFIX;
    } else if (type instanceof SimpleTypeBoolean) {
      suffix = ViewerConstants.SOLR_DYN_BOOLEAN;
    } else if (type instanceof SimpleTypeDateTime) {
      switch (viewerType.getDbType()) {
        case DATETIME_JUST_DATE:
          suffix = ViewerConstants.SOLR_DYN_TDATE;
          break;
        case DATETIME_JUST_TIME:
          suffix = ViewerConstants.SOLR_DYN_TTIME;
          break;
        case DATETIME:
        default:
          suffix = ViewerConstants.SOLR_DYN_TDATETIME;
      }
    } else if (type instanceof SimpleTypeEnumeration) {
      suffix = ViewerConstants.SOLR_DYN_STRING;
    } else if (type instanceof SimpleTypeInterval) {
      suffix = ViewerConstants.SOLR_DYN_DATES; // TODO: review chosen type
    } else if (type instanceof SimpleTypeNumericApproximate) {
      suffix = ViewerConstants.SOLR_DYN_DOUBLE;
    } else if (type instanceof SimpleTypeNumericExact) {
      SimpleTypeNumericExact exact = (SimpleTypeNumericExact) type;
      if (exact.getScale() > 0) {
        suffix = ViewerConstants.SOLR_DYN_DOUBLE;
      } else {
        suffix = ViewerConstants.SOLR_DYN_LONG;
      }
    } else if (type instanceof SimpleTypeString) {
      suffix = ViewerConstants.SOLR_DYN_TEXT_GENERAL;
    } else if (type instanceof ComposedTypeArray) {
      throw new ViewerException("Arrays are not yet supported.");
    } else if (type instanceof ComposedTypeStructure) {
      throw new ViewerException("Composed types are not yet supported.");
    } else {
      throw new ViewerException("Unknown type: " + type.toString());
    }

    return prefix + index + suffix;
  }

  private static ViewerType getType(Type type) throws ViewerException {
    ViewerType result = new ViewerType();

    if (!simpleMetadata) {

      if (type instanceof SimpleTypeBinary) {
        result.setDbType(ViewerType.dbTypes.BINARY);
      } else if (type instanceof SimpleTypeBoolean) {
        result.setDbType(ViewerType.dbTypes.BOOLEAN);
      } else if (type instanceof SimpleTypeDateTime) {
        if ("TIME WITH TIME ZONE".equalsIgnoreCase(type.getSql2008TypeName())
          || "TIME".equalsIgnoreCase(type.getSql2008TypeName())) {
          // solr does not have a time type, use string
          result.setDbType(ViewerType.dbTypes.DATETIME_JUST_TIME);
        } else if ("DATE".equalsIgnoreCase(type.getSql2008TypeName())) {
          result.setDbType(ViewerType.dbTypes.DATETIME_JUST_DATE);
        } else {
          result.setDbType(ViewerType.dbTypes.DATETIME);
        }
      } else if (type instanceof SimpleTypeEnumeration) {
        result.setDbType(ViewerType.dbTypes.ENUMERATION);
      } else if (type instanceof SimpleTypeInterval) {
        result.setDbType(ViewerType.dbTypes.TIME_INTERVAL);
      } else if (type instanceof SimpleTypeNumericApproximate) {
        result.setDbType(ViewerType.dbTypes.NUMERIC_FLOATING_POINT);
      } else if (type instanceof SimpleTypeNumericExact) {
        if (((SimpleTypeNumericExact) type).getScale() == 0) {
          result.setDbType(ViewerType.dbTypes.NUMERIC_INTEGER);
        } else {
          result.setDbType(ViewerType.dbTypes.NUMERIC_FLOATING_POINT);
        }
      } else if (type instanceof SimpleTypeString) {
        if ("CHARACTER LARGE OBJECT".equalsIgnoreCase(type.getSql2008TypeName())) {
          result.setDbType(ViewerType.dbTypes.CLOB);
        } else {
          result.setDbType(ViewerType.dbTypes.STRING);
        }
      } else if (type instanceof ComposedTypeArray) {
        result = new ViewerTypeArray();
        result.setDbType(ViewerType.dbTypes.COMPOSED_ARRAY);
        // set type of elements in the array
        ((ViewerTypeArray) result).setElementType(getType(((ComposedTypeArray) type).getElementType()));
      } else if (type instanceof ComposedTypeStructure) {
        result = new ViewerTypeStructure();
        result.setDbType(ViewerType.dbTypes.COMPOSED_STRUCTURE);
      } else {
        throw new ViewerException("Unknown type: " + type.toString());
      }
    }

    result.setDescription(type.getDescription());
    result.setTypeName(type.getSql2008TypeName());
    result.setOriginalTypeName(type.getOriginalTypeName());

    return result;
  }

  public static ViewerRow getRow(CollectionStatus collectionConfiguration, ViewerTable table, Row row, long rowIndex,
    String databasePath) {
    setCurrentTable(table);

    ViewerRow result = new ViewerRow();
    // String rowUUID = SolrUtils.UUIDFromString(table.getUuid() + "." + rowIndex);
    String rowUUID = String.valueOf(rowIndex);
    result.setTableId(table.getId());
    result.setTableUUID(table.getUuid());
    result.setUuid(rowUUID);
    result.setCells(getCells(collectionConfiguration, table, row, databasePath, result));
    return result;
  }

  private static void setCurrentTable(ViewerTable table) {
    if (StringUtils.isAllBlank(currentTable) || (!currentTable.equals(table.getId()))) {
      currentTable = table.getId();
    }
  }

  private static Map<String, ViewerCell> getCells(CollectionStatus collectionConfiguration, ViewerTable table, Row row,
    String databasePath, ViewerRow actualViewerRow) {
    Map<String, ViewerCell> result = new LinkedHashMap<>();

    int colIndex = 0;
    List<Cell> toolkitCells = row.getCells();
    for (ViewerColumn viewerColumn : table.getColumns()) {
      String solrColumnName = viewerColumn.getSolrName();
      try {
        result.put(solrColumnName, getCell(collectionConfiguration, table, toolkitCells.get(colIndex), colIndex++,
          databasePath, actualViewerRow));
      } catch (ViewerException e) {
        LOGGER.error("Problem converting cell, omitted it (as if it were NULL)", e);
      }
    }

    return result;
  }

  private static ViewerCell getCell(CollectionStatus collectionConfiguration, ViewerTable table, Cell cell,
    int colIndex, String databasePath, ViewerRow actualViewerRow) throws ViewerException {
    ViewerCell result = new ViewerCell();

    ViewerType columnType = table.getColumns().get(colIndex).getType();

    if (cell instanceof BinaryCell) {
      BinaryCell binaryCell = (BinaryCell) cell;
      if (binaryCell.getInputStreamProvider() instanceof TemporaryPathInputStreamProvider) {
        // BLOB is internal to the SIARD and is stored inside table.xml
        TemporaryPathInputStreamProvider temporaryPathInputStreamProvider = (TemporaryPathInputStreamProvider) binaryCell
          .getInputStreamProvider();
        try {
          final InputStream inputStream = temporaryPathInputStreamProvider.createInputStream();
          byte[] bytes = IOUtils.toByteArray(inputStream);
          final String encodeBase64String = Base64.encodeBase64String(bytes);
          result.setValue(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX + encodeBase64String);
          collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex)
            .setExternalLob(false);
          String index = getRowIndex(cell.getId());
          String lobName = ViewerConstants.SIARD_RECORD_PREFIX + index + ViewerConstants.SIARD_LOB_FILE_EXTENSION;
          actualViewerRow.addLobType(
              collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex).getId(),
              ViewerLobStoreType.EXTERNALLY);

          detectMimeType(actualViewerRow, result, databasePath, collectionConfiguration, table, colIndex, lobName,
            true);

        } catch (ModuleException e) {
          throw new ViewerException(e.getMessage(), e);
        } catch (IOException e) {
          throw new ViewerException("Could not convert the LOB to BASE64", e);
        }
      } else if (binaryCell.getInputStreamProvider() instanceof PathInputStreamProvider) {
        // BLOB is external to the SIARD

        PathInputStreamProvider pathInputStreamProvider = (PathInputStreamProvider) binaryCell.getInputStreamProvider();
        final Path siardFilesPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath();
        final Path lobPath = pathInputStreamProvider.getPath();
        String index = getRowIndex(cell.getId());
        String lobName = ViewerConstants.SIARD_RECORD_PREFIX + index + ViewerConstants.SIARD_LOB_FILE_EXTENSION;
        result.setValue(siardFilesPath.relativize(lobPath).normalize().toString());
        collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex).setExternalLob(true);
        actualViewerRow.addLobType(
            collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex).getId(),
            ViewerLobStoreType.EXTERNALLY);

        detectMimeType(actualViewerRow, result, databasePath, collectionConfiguration, table, colIndex, lobName, false);

      } else {
        // BLOB is internal to the SIARD but is stored outside the table.xml (Normal)
        
        String lobName = Paths.get(binaryCell.getFile()).getFileName().toString();
        result.setValue(lobName);
        collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex).setExternalLob(false);
        actualViewerRow.addLobType(
          collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex).getId(),
          ViewerLobStoreType.INTERNALLY);

        detectMimeType(actualViewerRow, result, databasePath, collectionConfiguration, table, colIndex, lobName, true);
      }
    } else if (cell instanceof ComposedCell) {
      ComposedCell composedCell = (ComposedCell) cell;
      LOGGER.debug("composed cell not supported yet");
      // TODO: composed cell
    } else if (cell instanceof SimpleCell) {
      SimpleCell simpleCell = (SimpleCell) cell;
      String simpleData = simpleCell.getSimpleData();

      switch (columnType.getDbType()) {
        case DATETIME:
          result.setValue(JodaUtils.xsDatetimeParse(simpleData).withZone(DateTimeZone.UTC).toString());
          break;
        case DATETIME_JUST_DATE:
          result.setValue(JodaUtils.xsDateParse(simpleData).withTime(0, 0, 0, 0).withZone(DateTimeZone.UTC).toString());
          break;
        case DATETIME_JUST_TIME:
          result.setValue(JodaUtils.xsTimeParse(simpleData).withDate(1970, 1, 1).withZone(DateTimeZone.UTC).toString());
          break;
        default:
          result.setValue(removeUnicode(simpleCell.getSimpleData()));
      }
    } else if (!(cell instanceof NullCell)) {
      // nothing to do for null cells
      throw new ViewerException("Unexpected cell type");
    }

    return result;
  }

  private static void detectMimeType(ViewerRow row, ViewerCell cell, String databasePath,
    CollectionStatus collectionConfiguration, ViewerTable table, int colIndex, String lobName,
    boolean blobIsInsideSiard) {
    try {
      String mimeType;
      String fileExtension;
      InputStream inputStream;

      TableStatus tableStatus = collectionConfiguration.getTableStatusByTableId(table.getId());
      String siardLobPath = LobManagerUtils.getZipFilePath(tableStatus, colIndex, lobName);

      ZipFile zipFile = new ZipFile(databasePath);
      ZipEntry entry = zipFile.getEntry(siardLobPath);

      String lobCellValue = cell.getValue();

      if (entry != null && blobIsInsideSiard) {
        inputStream = zipFile.getInputStream(entry);
      } else if (blobIsInsideSiard) {
        lobCellValue = lobCellValue.replace(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX, "");
        inputStream = new ByteArrayInputStream(Base64.decodeBase64(lobCellValue.getBytes()));
      } else {
        lobCellValue = cell.getValue();
        final Path lobPath = Paths.get(lobCellValue);
        final Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);
        inputStream = Files.newInputStream(completeLobPath);
      }

      mimeType = tika.detect(inputStream);
      fileExtension = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();

      if (StringUtils.isAllBlank(fileExtension)) {
        try {
          if (blobIsInsideSiard) {
            inputStream = zipFile.getInputStream(entry);
          } else {
            final Path lobPath = Paths.get(lobCellValue);
            final Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);
            inputStream = Files.newInputStream(completeLobPath);
          }

          AutoDetectParser parser = new AutoDetectParser();
          Metadata metadata = new Metadata();

          Boolean autoDetectParserNoLimit = ViewerFactory.getEnvBoolean("AUTO_DETECT_PARSER_NO_LIMIT", false);

          if (autoDetectParserNoLimit) {
            parser.parse(inputStream, new BodyContentHandler(-1), metadata, new ParseContext());
          } else {
            parser.parse(inputStream, new BodyContentHandler(), metadata, new ParseContext());
          }

          mimeType = metadata.get("Content-Type");
          fileExtension = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();

        } catch (SAXException | TikaException e) {
          LOGGER.error("Could not calculate mimeType for special extensions in the cell: [{}]", cell.getValue(), e);
        }
      }

      inputStream.close();
      zipFile.close();

      cell.setMimeType(mimeType);
      cell.setFileExtension(fileExtension);

      collectionConfiguration.updateColumnMimeType(table.getUuid(), colIndex);
      collectionConfiguration.updateLobFileName(table.getUuid(), colIndex);

      ViewerMimeType viewerMimeType = new ViewerMimeType(mimeType, fileExtension);
      String colName = collectionConfiguration.getTableStatusByTableId(table.getId()).getColumnByIndex(colIndex)
        .getId();
      row.addMimeTypeListEntry(colName, viewerMimeType);

    } catch (IOException | MimeTypeException e) {
      LOGGER.error("Could not calculate mimeType for cell: [" + cell.getValue() + "]", e);
    }
  }

  private static String getRowIndex(String cellId) throws ViewerException {
    final Matcher matcher = rowIndexPattern.matcher(cellId);
    if (matcher.matches()) {
      return matcher.group(2);
    }

    throw new ViewerException("Could not obtain row index for cell with id: " + cellId);
  }

  private static String removeUnicode(String string) {
    // remove any invisible control characters and unused code characters.
    // based on: http://stackoverflow.com/a/11021262/1483200
    // more info:
    // https://en.wikipedia.org/wiki/Unicode_character_property#General_Category
    return string.replaceAll("\\p{C}", "");
  }

  /**
   * Helper class to hold references to schemas and columns, even in different
   * schemas
   */
  private static class ReferenceHolder {
    // tableID -> (tableUUID, columnName -> columnIndex)
    private HashMap<String, Pair<String, HashMap<String, Integer>>> infoByTableID;

    /**
     * build references from the database
     *
     * @param database
     *          the database from DBPTK
     */
    public ReferenceHolder(DatabaseStructure database) {
      infoByTableID = new HashMap<>();
      for (SchemaStructure schema : database.getSchemas()) {
        for (TableStructure table : schema.getTables()) {
          String tableID = table.getId();
          String tableUUID = SolrUtils.randomUUID();

          int index = 0;
          HashMap<String, Integer> columnNamesAndIndexes = new HashMap<>();
          for (ColumnStructure column : table.getColumns()) {
            columnNamesAndIndexes.put(column.getName(), index++);
          }

          infoByTableID.put(tableID, new ImmutablePair<>(tableUUID, columnNamesAndIndexes));
        }
      }
    }

    public String getTableUUID(String schemaName, String tableName) {
      return getTableUUID(getIdFromNames(schemaName, tableName));
    }

    public String getTableUUID(String tableID) {
      if (infoByTableID.get(tableID) != null)
        return infoByTableID.get(tableID).getKey();

      return "";
    }

    public Integer getIndexForColumn(String tableID, String columnName) {
      if (infoByTableID.get(tableID) != null) {
        return infoByTableID.get(tableID).getValue().get(columnName);
      }
      return -1;
    }

    public Integer getIndexForColumn(String schemaName, String tableName, String columnName) {
      if (infoByTableID.get(getIdFromNames(schemaName, tableName)) != null) {
        return infoByTableID.get(getIdFromNames(schemaName, tableName)).getValue().get(columnName);
      }
      return -1;
    }

    private String getIdFromNames(String schemaName, String tableName) {
      return schemaName + "." + tableName;
    }
  }
}
