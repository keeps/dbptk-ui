package com.databasepreservation.dbviewer.transformers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.model.exception.ModuleException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.dbviewer.ViewerConstants;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerCell;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRow;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerType;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTypeArray;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTypeStructure;
import com.databasepreservation.dbviewer.exceptions.ViewerException;
import com.databasepreservation.dbviewer.utils.SolrUtils;
import com.databasepreservation.dbviewer.utils.ViewerUtils;
import com.databasepreservation.model.data.BinaryCell;
import com.databasepreservation.model.data.Cell;
import com.databasepreservation.model.data.ComposedCell;
import com.databasepreservation.model.data.NullCell;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.data.SimpleCell;
import com.databasepreservation.model.structure.ColumnStructure;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
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
    ViewerDatabaseFromToolkit result = new ViewerDatabaseFromToolkit();
    result.setUuid(SolrUtils.randomUUID());
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

    result.setArchivalDate(getArchivalDate(structure));
    result.setSchemas(getSchemas(vdb, structure.getSchemas()));
    return result;
  }

  private static String getDatabaseProduct(DatabaseStructure structure) {
    if(StringUtils.isNotBlank(structure.getProductVersion())) {
      return structure.getProductName() + " v" + structure.getProductVersion();
    }else{
      return structure.getProductName();
    }
  }

  private static String getArchivalDate(DatabaseStructure structure) {
    return ViewerUtils.dateToString(structure.getArchivalDate().withZone(DateTimeZone.UTC).toDate());
  }

  private static List<ViewerSchema> getSchemas(ViewerDatabaseFromToolkit vdb, List<SchemaStructure> schemas)
    throws ViewerException {
    List<ViewerSchema> result = new ArrayList<>();
    for (SchemaStructure schema : schemas) {
      result.add(getSchema(vdb, schema));
    }
    return result;
  }

  private static ViewerSchema getSchema(ViewerDatabaseFromToolkit vdb, SchemaStructure schema) throws ViewerException {
    ViewerSchema result = new ViewerSchema();
    result.setName(schema.getName());
    result.setDescription(schema.getDescription());
    result.setTables(getTables(vdb, schema.getTables()));

    vdb.putSchema(schema.getName(), result);
    return result;
  }

  private static List<ViewerTable> getTables(ViewerDatabaseFromToolkit vdb, List<TableStructure> tables)
    throws ViewerException {
    List<ViewerTable> result = new ArrayList<>();
    for (TableStructure table : tables) {
      result.add(getTable(vdb, table));
    }
    return result;
  }

  private static ViewerTable getTable(ViewerDatabaseFromToolkit vdb, TableStructure table) throws ViewerException {
    ViewerTable result = new ViewerTable();
    result.setUuid(SolrUtils.randomUUID());
    result.setName(table.getName());
    result.setDescription(table.getDescription());
    result.setCountRows(table.getRows());
    result.setSchema(table.getSchema());
    result.setColumns(getColumns(table.getColumns()));

    vdb.putTable(table.getId(), result);
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

    result.setDisplayName(column.getName());
    result.setSolrName(getColumnSolrName(index, column.getType()));
    result.setDescription(column.getDescription());
    result.setAutoIncrement(column.getIsAutoIncrement());
    result.setDefaultValue(column.getDefaultValue());
    result.setNillable(column.getNillable());
    result.setType(getType(column.getType()));

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
  private static String getColumnSolrName(int index, Type type) throws ViewerException {
    // suffix must always be set before being used
    String suffix;

    if (type instanceof SimpleTypeBinary) {
      suffix = ViewerConstants.SOLR_DYN_STRING;
    } else if (type instanceof SimpleTypeBoolean) {
      suffix = ViewerConstants.SOLR_DYN_BOOLEAN;
    } else if (type instanceof SimpleTypeDateTime) {
      suffix = ViewerConstants.SOLR_DYN_TDATE;
    } else if (type instanceof SimpleTypeEnumeration) {
      suffix = ViewerConstants.SOLR_DYN_STRING;
    } else if (type instanceof SimpleTypeInterval) {
      suffix = ViewerConstants.SOLR_DYN_TDATES; // TODO: review chosen type
    } else if (type instanceof SimpleTypeNumericApproximate) {
      suffix = ViewerConstants.SOLR_DYN_TDOUBLE;
    } else if (type instanceof SimpleTypeNumericExact) {
      SimpleTypeNumericExact exact = (SimpleTypeNumericExact) type;
      if (exact.getScale() > 0) {
        suffix = ViewerConstants.SOLR_DYN_TDOUBLE;
      } else {
        suffix = ViewerConstants.SOLR_DYN_TLONG;
      }
    } else if (type instanceof SimpleTypeString) {
      suffix = ViewerConstants.SOLR_DYN_STRING;
    } else if (type instanceof ComposedTypeArray) {
      throw new ViewerException("Arrays are not yet supported.");
    } else if (type instanceof ComposedTypeStructure) {
      throw new ViewerException("Composed types are not yet supported.");
    } else {
      throw new ViewerException("Unknown type: " + type.toString());
    }

    return ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX + index + suffix;
  }

  private static ViewerType getType(Type type) throws ViewerException {
    ViewerType result = new ViewerType();

    if (type instanceof SimpleTypeBinary) {
      result.setDbType(ViewerType.dbTypes.BINARY);
    } else if (type instanceof SimpleTypeBoolean) {
      result.setDbType(ViewerType.dbTypes.BOOLEAN);
    } else if (type instanceof SimpleTypeDateTime) {
      result.setDbType(ViewerType.dbTypes.DATETIME);
    } else if (type instanceof SimpleTypeEnumeration) {
      result.setDbType(ViewerType.dbTypes.ENUMERATION);
    } else if (type instanceof SimpleTypeInterval) {
      result.setDbType(ViewerType.dbTypes.INTERVAL);
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

  public static ViewerRow getRow(ViewerTable table, Row row, long rowIndex) throws ViewerException {
    ViewerRow result = new ViewerRow();
    result.setUUID(SolrUtils.randomUUID());
    result.setCells(getCells(table, row, rowIndex));
    return result;
  }

  private static Map<String, ViewerCell> getCells(ViewerTable table, Row row, long rowIndex) throws ViewerException {
    HashMap<String, ViewerCell> result = new HashMap<>();

    int colIndex = 0;
    List<Cell> toolkitCells = row.getCells();
    for (ViewerColumn viewerColumn : table.getColumns()) {
      String solrColumnName = viewerColumn.getSolrName();
      result.put(solrColumnName, getCell(table, toolkitCells.get(colIndex), rowIndex, colIndex++));
    }

    return result;
  }

  private static ViewerCell getCell(ViewerTable table, Cell cell, long rowIndex, int colIndex) throws ViewerException {
    ViewerCell result = new ViewerCell();
    if (cell instanceof BinaryCell) {
      BinaryCell binaryCell = (BinaryCell) cell;

      String lobFilename = "blob"+colIndex+"_"+rowIndex+".bin";

      // copy blob to a file at <USER_DBVIEWER_DIR>/<table_UUID>/blob<column_index>_<row_index>.bin
      try {
        Path outputPath = ViewerConstants.USER_DBVIEWER_DIR.resolve(table.getUUID()+"/");
        outputPath = Files.createDirectories(outputPath);
        outputPath = outputPath.resolve(lobFilename);
        InputStream stream = binaryCell.createInputstream();
        Files.copy(stream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        try {
          stream.close();
        }catch(IOException e){
          LOGGER.debug("could not close binaryCell input stream", e);
        }

        try {
          binaryCell.cleanResources();
        }catch(IOException e){
          LOGGER.debug("could not free binary cell resources", e);
        }
      } catch (IOException | ModuleException e) {
        throw new ViewerException("Could not copy blob to user directory");
      }

      result.setValue(lobFilename);
    } else if (cell instanceof ComposedCell) {
      ComposedCell composedCell = (ComposedCell) cell;
      LOGGER.debug("composed cell not supported yet");
      // TODO: composed cell
    } else if (cell instanceof SimpleCell) {
      SimpleCell simpleCell = (SimpleCell) cell;
      result.setValue(simpleCell.getSimpleData());
    } else if (cell instanceof NullCell) {
      // nothing to do for null cells
    } else {
      throw new ViewerException("Unexpected cell type");
    }


    return result;
  }
}
