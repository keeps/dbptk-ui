package com.databasepreservation.common.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.Pair;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.*;
import com.databasepreservation.common.shared.client.tools.FilterUtils;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DbvtkDenormalizationDatabase {
  private final DatabaseRowsSolrManager solrManager;
  private ViewerDatabase database;
  private ViewerMetadata metadata;
  private String databaseUUID;

  public DbvtkDenormalizationDatabase(String databaseUUID) throws ModuleException {
    solrManager = ViewerFactory.getSolrManager();
    this.databaseUUID = databaseUUID;
    try {
      database = solrManager.retrieve(ViewerDatabase.class, this.databaseUUID);
    } catch (NotFoundException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }

  }

  public void denormalize() throws ModuleException{
    metadata = database.getMetadata();
    getTables();
    solrManager.updateDatabaseMetadata(databaseUUID, metadata);
  }

  private void getTables() throws ModuleException{
    for (Map.Entry<String, ViewerTable> entry : metadata.getTables().entrySet()) {
      ViewerTable table = entry.getValue();
      getForeignKeys(table);
    }
  }

  private void getForeignKeys(ViewerTable table) throws ModuleException{
    for (ViewerForeignKey foreignKey : table.getForeignKeys()) {
      ViewerTable referencedTable = metadata.getTable(foreignKey.getReferencedTableUUID());
      if(referencedTable.getColumns().size() > 2){
        // next foreign key
        continue;
      }
      getReferences(table, referencedTable, foreignKey.getReferences());
    }
  }

  private void getReferences(ViewerTable table, ViewerTable referencedTable, List<ViewerReference> references) throws ModuleException{
    for(ViewerReference reference : references){
      ViewerColumn sourceColumn = table.getColumns().get(reference.getSourceColumnIndex());
      ViewerColumn referencedColumn = referencedTable.getColumns().get(reference.getReferencedColumnIndex());
      getRows(table, referencedTable, sourceColumn, referencedColumn);

      List<ViewerColumn> columns = table.getColumns();
      List<ViewerColumn> listColumns = referencedTable.getColumns();
      for(ViewerColumn column: listColumns){
        if(!column.getSolrName().equals(referencedColumn.getSolrName())){
          ViewerColumn rColumn = new ViewerColumn();
          rColumn.setSolrName("r_" + sourceColumn.getSolrName() + "." + column.getSolrName());
          rColumn.setDisplayName(referencedTable.getName() + "." + column.getDisplayName());
          rColumn.setDescription(column.getDescription());
          rColumn.setAutoIncrement(column.getAutoIncrement());
          rColumn.setColumnIndexInEnclosingTable(column.getColumnIndexInEnclosingTable());
          rColumn.setDefaultValue(column.getDefaultValue());
          rColumn.setNillable(column.getNillable());
          rColumn.setType(column.getType());
          columns.add(rColumn);
        }
      }
      metadata.getTableById(table.getId()).setColumns(columns);
    }
  }

  private void getRows(ViewerTable table, ViewerTable referencedTable, ViewerColumn sourceColumn, ViewerColumn referencedColumn) throws ModuleException{
    Filter filter = FilterUtils.filterByTable(new Filter(), table.getId());
    IterableIndexResult sourceRows = solrManager.findAllRows(databaseUUID, filter, null, Arrays.asList(ViewerConstants.INDEX_ID, sourceColumn.getSolrName()));
    for(ViewerRow row : sourceRows){
      ViewerCell cell = row.getCells().get(sourceColumn.getSolrName());
      if(cell == null ) {
        // next row
        continue;
      }

      getReferencedRows(referencedTable, referencedColumn, row.getUUID(), cell.getValue(), table, sourceColumn, row);
    }
  }

  private void getReferencedRows(ViewerTable referencedTable, ViewerColumn referencedColumn, String documentUUID,
    String value, ViewerTable table, ViewerColumn sourceColumn, ViewerRow row) throws ModuleException {
    Filter filter = new Filter(
        new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, referencedTable.getId()),
        new SimpleFilterParameter(referencedColumn.getSolrName(), value));
    Sublist sublist = new Sublist(0, 1);

    try {
      ViewerRow referencedRow = solrManager.findRows(databaseUUID, filter, null, sublist, null).getResults().get(0);
      Map<String, ViewerCell> cells = referencedRow.getCells();

      for(Map.Entry<String, ViewerCell> cell : cells.entrySet()) {
        if(!cell.getKey().equals(referencedColumn.getSolrName())){
          String key = "r_" + sourceColumn.getSolrName() + "." + cell.getKey();
          ViewerCell cellValue = cell.getValue();
//          System.out.println("uuid:" + referencedRow.getTableId() + " " + key + " = " + cellValue.getValue());
          String uuid = row.getUUID() + "." + referencedTable.getId() + "." + referencedColumn.getSolrName();
          //solrManager.addDatabaseField(databaseUUID, documentUUID, SolrUtils.UUIDFromString(uuid),Pair.of(key, cellValue.getValue()));
          solrManager.addDatabaseField(databaseUUID, documentUUID, Pair.of(key, cellValue.getValue()));
        }
      }

    } catch (GenericException | RequestNotValidException e) {
      throw new ModuleException().withMessage("Cannot retrieved row from solr");
    }
  }
}
