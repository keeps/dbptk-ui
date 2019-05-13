package com.databasepreservation.visualization.transformers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.model.data.BinaryCell;
import com.databasepreservation.model.data.Cell;
import com.databasepreservation.model.data.ComposedCell;
import com.databasepreservation.model.data.NullCell;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.data.SimpleCell;
import com.databasepreservation.model.exception.ModuleException;
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
import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.index.utils.SolrUtils;
import com.databasepreservation.visualization.shared.ViewerConstants;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerCell;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerCheckConstraint;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerReference;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerRoutine;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerRoutineParameter;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTrigger;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerType;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTypeArray;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTypeStructure;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerView;
import com.databasepreservation.visualization.utils.LobPathManager;
import com.databasepreservation.visualization.utils.ViewerAbstractConfiguration;
import com.databasepreservation.visualization.utils.ViewerUtils;

/**
 * Utility class used to convert a DatabaseStructure (used in Database
 * Preservation Toolkit) to a ViewerStructure (used in Database Viewer)
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ToolkitStructure2ViewerStructure {
  private static final Logger LOGGER = LoggerFactory.getLogger(ToolkitStructure2ViewerStructure.class);

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
    return getDatabase(structure, SolrUtils.randomUUID());
  }

  public static ViewerDatabaseFromToolkit getDatabase(DatabaseStructure structure, String databaseUUID)
    throws ViewerException {
    ViewerDatabaseFromToolkit result = new ViewerDatabaseFromToolkit();
    result.setUUID(databaseUUID);
    result.setStatus(ViewerDatabase.Status.INGESTING);
    result.setMetadata(getMetadata(result, structure));
    return result;
  }

  private static ViewerMetadata getMetadata(ViewerDatabaseFromToolkit vdb, DatabaseStructure structure)
    throws ViewerException {
    ViewerMetadata result = new ViewerMetadata();
    result.setName(structure.getName());
    result.setArchiver(structure.getArchiver());
    result.setArchiverContact(structure.getArchiverContact());
    result.setClientMachine(structure.getClientMachine());
    result.setDatabaseProduct(getDatabaseProduct(structure));
    result.setDatabaseUser(structure.getDatabaseUser());
    result.setDataOriginTimespan(structure.getDataOriginTimespan());
    result.setDataOwner(structure.getDataOwner());
    result.setDescription(structure.getDescription());
    result.setProducerApplication(structure.getProducerApplication());

    result.setUsers(getUsers(structure.getUsers()));
    result.setRoles(getRoles(structure.getRoles()));
    result.setPrivileges(getPrivileges(structure.getPrivileges()));

    result.setArchivalDate(getArchivalDate(structure));

    ReferenceHolder references = new ReferenceHolder(structure);

    result.setSchemas(getSchemas(vdb, structure.getSchemas(), references));
    return result;
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
    result.setUUID(SolrUtils.randomUUID());
    result.setName(schema.getName());
    result.setDescription(schema.getDescription());

    result.setRoutines(getRoutines(schema.getRoutines()));
    result.setViews(getViews(schema.getViews()));

    vdb.putSchema(schema.getName(), result);
    result.setTables(getTables(vdb, schema.getTables(), references));

    return result;
  }

  private static List<ViewerView> getViews(List<ViewStructure> views) {
    ArrayList<ViewerView> result = new ArrayList<>();
    if (views != null) {
      for (ViewStructure view : views) {
        result.add(getView(view));
      }
    }
    return result;
  }

  private static ViewerView getView(ViewStructure view) {
    ViewerView result = new ViewerView();
    result.setName(view.getName());
    try {
      result.setColumns(getColumns(view.getColumns()));
    } catch (ViewerException e) {
      LOGGER.error("Could not convert the columns for view " + view.toString(), e);
      result.setColumns(new ArrayList<ViewerColumn>());
    }

    result.setQuery(view.getQuery());
    result.setQueryOriginal(view.getQueryOriginal());
    result.setDescription(view.getDescription());

    return result;
  }

  private static List<ViewerRoutine> getRoutines(List<RoutineStructure> routines) {
    ArrayList<ViewerRoutine> result = new ArrayList<>();
    for (RoutineStructure routine : routines) {
      result.add(getRoutine(routine));
    }
    return result;
  }

  private static ViewerRoutine getRoutine(RoutineStructure routine) {
    ViewerRoutine result = new ViewerRoutine();
    result.setName(routine.getName());

    result.setDescription(routine.getDescription());
    result.setSource(routine.getSource());
    result.setBody(routine.getBody());
    result.setCharacteristic(routine.getCharacteristic());
    result.setReturnType(routine.getReturnType());

    result.setParameters(getRoutineParameters(routine.getParameters()));

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
    result.setCountRows(table.getRows());
    result.setSchemaName(table.getSchema());
    result.setSchemaUUID(vdb.getSchema(result.getSchemaName()).getUUID());
    result.setColumns(getColumns(table.getColumns()));
    result.setTriggers(getTriggers(table.getTriggers()));
    result.setPrimaryKey(getPrimaryKey(table, references));
    result.setForeignKeys(getForeignKeys(table, references));
    result.setCheckConstraints(getCheckConstraints(table.getCheckConstraints()));

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
    result.setDescription(constraint.getCondition());
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

    result.setDisplayName(column.getName());
    result.setType(columnViewerType);
    result.setSolrName(getColumnSolrName(index, columnType, columnViewerType));
    result.setColumnIndexInEnclosingTable(index);
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
      result.setDbType(ViewerType.dbTypes.STRING);
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

    result.setDescription(type.getDescription());
    result.setTypeName(type.getSql2008TypeName());
    result.setOriginalTypeName(type.getOriginalTypeName());

    return result;
  }

  public static ViewerRow getRow(ViewerAbstractConfiguration configuration, String databaseUUID, ViewerTable table,
    Row row, long rowIndex) throws ViewerException {
    ViewerRow result = new ViewerRow();
    String rowUUID = SolrUtils.UUIDFromString(table.getId() + "." + rowIndex);
    result.setTableId(table.getId());
    result.setUUID(rowUUID);
    result.setCells(getCells(configuration, databaseUUID, table, row, rowIndex, rowUUID));
    return result;
  }

  private static Map<String, ViewerCell> getCells(ViewerAbstractConfiguration configuration, String databaseUUID,
    ViewerTable table, Row row, long rowIndex, String rowUUID) throws ViewerException {
    HashMap<String, ViewerCell> result = new HashMap<>();

    int colIndex = 0;
    List<Cell> toolkitCells = row.getCells();
    for (ViewerColumn viewerColumn : table.getColumns()) {
      String solrColumnName = viewerColumn.getSolrName();
      try {
        result.put(solrColumnName,
          getCell(configuration, databaseUUID, table, toolkitCells.get(colIndex), rowIndex, colIndex++, rowUUID));
      } catch (ViewerException e) {
        LOGGER.error("Problem converting cell, omitted it (as if it were NULL)", e);
      }
    }

    return result;
  }

  private static ViewerCell getCell(ViewerAbstractConfiguration configuration, String databaseUUID, ViewerTable table,
    Cell cell, long rowIndex, int colIndex, String rowUUID) throws ViewerException {
    ViewerCell result = new ViewerCell();

    ViewerType columnType = table.getColumns().get(colIndex).getType();

    if (cell instanceof BinaryCell) {
      BinaryCell binaryCell = (BinaryCell) cell;

      InputStream stream = null;
      try {
        Path outputPath = LobPathManager.getPath(configuration, databaseUUID, table.getUUID(), colIndex, rowUUID);
        Files.createDirectories(outputPath.getParent());
        stream = binaryCell.createInputStream();
        Files.copy(stream, outputPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException | ModuleException e) {
        throw new ViewerException("Could not copy blob", e);
      } finally {
        IOUtils.closeQuietly(stream);
        binaryCell.cleanResources();
      }

      result.setValue("dummy");
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

  private static String removeUnicode(String string) {
    // remove any invisible control characters and unused code characters.
    // based on: http://stackoverflow.com/a/11021262/1483200
    // more info:
    // https://en.wikipedia.org/wiki/Unicode_character_property#General_Category
    return string.replaceAll("\\p{C}", "");
  }

  /**
   * Helper class to hold references to tables and columns, even in different
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
      return infoByTableID.get(tableID).getKey();
    }

    public Integer getIndexForColumn(String tableID, String columnName) {
      return infoByTableID.get(tableID).getValue().get(columnName);
    }

    public Integer getIndexForColumn(String schemaName, String tableName, String columnName) {
      return infoByTableID.get(getIdFromNames(schemaName, tableName)).getValue().get(columnName);
    }

    private String getIdFromNames(String schemaName, String tableName) {
      return schemaName + "." + tableName;
    }
  }
}
