package com.databasepreservation.common.transformers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.ReferencesConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.filter.solr.TermsFilterParameter;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.utils.JodaUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeSolrStructure {
  private final DatabaseRowsSolrManager solrManager;
  private ViewerDatabase database;
  private String databaseUUID;
  private CollectionConfiguration configuration;
  private DenormalizeConfiguration denormalizeConfiguration;

  public DenormalizeSolrStructure(String databaseUUID) throws ModuleException {
    solrManager = ViewerFactory.getSolrManager();
    this.databaseUUID = databaseUUID;
    try {
      database = solrManager.retrieve(ViewerDatabase.class, this.databaseUUID);
      denormalize();
    } catch (NotFoundException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }
  }

  public void denormalize() throws ModuleException {
    Path configurationPath = ViewerConfiguration.getInstance().getDatabaseConfigPath()
      .resolve(database.getUuid() + ViewerConstants.JSON_EXTENSION);
    if (Files.exists(configurationPath)) {
      configuration = JsonTransformer.readObjectFromFile(configurationPath, CollectionConfiguration.class);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist");
    }

    for (TableConfiguration tables : configuration.getTables()) {
      ViewerTable table = database.getMetadata().getTable(tables.getUuid());
      Path denormalizeConfigurationPath = ViewerConfiguration.getInstance().getDatabaseConfigPath()
          .resolve(database.getUuid() + "." + table.getUUID() + "-CURRENT" + ViewerConstants.JSON_EXTENSION);

      if (Files.exists(denormalizeConfigurationPath)) {
        denormalizeConfiguration = JsonTransformer.readObjectFromFile(denormalizeConfigurationPath, DenormalizeConfiguration.class);
      } else {
        throw new ModuleException().withMessage("Configuration file not exist");
      }
      getRowsToDenormalize(denormalizeConfiguration);
    }

    System.out.println("denormalize() ended");
  }

  private void getRowsToDenormalize(DenormalizeConfiguration item) throws ModuleException {
    Filter filter = FilterUtils.filterByTable(new Filter(), item.getTableID());
    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    for (RelatedTablesConfiguration relatedTableList : item.getRelatedTables()) {
      if (relatedTableList.getReferencedTableUUID().equals(item.getTableUUID())) {
        for (ReferencesConfiguration references : relatedTableList.getReferences()) {
          fieldsToReturn.add(references.getReferencedTable().getSolrName());
        }
      }

      IterableIndexResult sourceRows = solrManager.findAllRows(databaseUUID, filter, null, fieldsToReturn);
      for (ViewerRow row : sourceRows) {
        buildMainQuery(item, row);
      }
    }
  }

  private void buildMainQuery(DenormalizeConfiguration item, ViewerRow row) throws ModuleException {
    Map<String, ViewerCell> cells = row.getCells();
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();
    List<SolrQuery> queryList = new ArrayList<>();
    for (Map.Entry<String, ViewerCell> entry : cells.entrySet()) {
      filterParameterList.add(new AndFiltersParameters(
        Arrays.asList(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, item.getTableID()),
          new SimpleFilterParameter(entry.getKey(), entry.getValue().getValue()))));
    }
    resultingFilter.add(filterParameterList);

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add("*");

    List<RelatedTablesConfiguration> relatedTableList = item.getRelatedTables();
    for (RelatedTablesConfiguration relatedTable : relatedTableList) {
      // if table is referenced by some of related tables, add a sub query
      if (relatedTable.getReferencedTableUUID().equals(item.getTableUUID())) {
        fieldsToReturn.add("nested:[subquery]");
        break;
      }
    }

    try {
      SolrQuery entries = SolrUtils.buildQuery(0, resultingFilter, fieldsToReturn);
      queryList.add(entries);
      Map<String, List<String>> fieldsToDisplay = new HashMap<>();
      for (RelatedTablesConfiguration table : item.getRelatedTables()) {
        queryList.add(buildSubQuery(item, table));

        if (fieldsToDisplay.get(table.getTableUUID()) == null) {
          fieldsToDisplay.put(table.getTableUUID(), new ArrayList<>());
        }
        fieldsToDisplay.get(table.getTableUUID()).addAll(buildColumnsToDisplay(table));
      }

      ViewerRow document = solrManager.findRows(databaseUUID, queryList).getResults().get(0);

      List<SolrInputDocument> nestedDocument = new ArrayList<>();

      if (document.getNestedRowList() != null) {
        buildNestedDocumentList(document.getNestedRowList(), nestedDocument, document.getUuid(), document.getTableId(),
          fieldsToDisplay, item);
      }

      if (!nestedDocument.isEmpty()) {
        solrManager.addDatabaseField(databaseUUID, document.getUuid(), nestedDocument);
      }

    } catch (RequestNotValidException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved row from solr");
    }
  }

  private SolrQuery buildSubQuery(DenormalizeConfiguration item, RelatedTablesConfiguration table)
    throws RequestNotValidException {
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();

    for (ReferencesConfiguration reference : table.getReferences()) {
      String sourceSolrName = reference.getReferencedTable().getSolrName();
      String referencedSolrName = reference.getSourceTable().getSolrName();

      filterParameterList.add(new AndFiltersParameters(
        Arrays.asList(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, table.getTableID()),
          new TermsFilterParameter(referencedSolrName, "$row." + sourceSolrName))));
    }

    resultingFilter.add(filterParameterList);
    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add("*");

    List<RelatedTablesConfiguration> relatedTableList = item.getRelatedTables();
    for (RelatedTablesConfiguration relatedTable : relatedTableList) {
      // if table is referenced by some of related tables, add a sub query
      if (relatedTable.getReferencedTableUUID().equals(table.getTableUUID())) {
        fieldsToReturn.add("nested:[subquery]");
        break;
      }
    }

    return SolrUtils.buildQuery(0, resultingFilter, fieldsToReturn);
  }

  private void buildNestedDocumentList(List<ViewerRow> documentList, List<SolrInputDocument> nestedDocument,
                                       String uuid, String referenceTableId, Map<String, List<String>> fieldsToDisplay, DenormalizeConfiguration item) {
    for (ViewerRow document : documentList) {
      Map<String, ViewerCell> cells = document.getCells();
      String nestedUUID = uuid + "." + document.getUuid();

      Map<String, Object> fields = new HashMap<>();
      for (Map.Entry<String, ViewerCell> cell : cells.entrySet()) {
        String key = cell.getKey();
        if(fieldsToDisplay.get(document.getTableUUID()) != null && !fieldsToDisplay.get(document.getTableUUID()).contains(key)){
          continue;
        }
        ViewerCell cellValue = cell.getValue();
        if (key.endsWith(ViewerConstants.SOLR_DYN_DATE)) {
          fields.put(key, JodaUtils.xsDateParse(cellValue.getValue()).toString());
        } else {
          fields.put(key, cellValue.getValue());
        }
      }
      if (!fields.isEmpty()) {
        nestedDocument
          .add(solrManager.createNestedDocument(nestedUUID, fields, referenceTableId, document.getTableId(), item));
      }

      if (document.getNestedRowList() == null) {
        return;
      } else {
        buildNestedDocumentList(document.getNestedRowList(), nestedDocument, nestedUUID, document.getTableId(), fieldsToDisplay, item);
      }
    }
  }


  private static List<String> buildColumnsToDisplay(RelatedTablesConfiguration table) {
    List<String> displayColumns = new ArrayList<>();

    for(RelatedColumnConfiguration columnsIncluded : table.getColumnsIncluded()){
      displayColumns.add(columnsIncluded.getSolrName());
    }

    return displayColumns;
  }
}
