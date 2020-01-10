package com.databasepreservation.common.transformers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.utils.StatusUtils;
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
import com.databasepreservation.common.client.common.utils.Tree;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.ReferencesConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.filter.solr.TermsFilterParameter;
import com.databasepreservation.common.server.DataTransformationObserver;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.IterableNestedIndexResult;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.utils.JodaUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class Denormalize {
  private final DatabaseRowsSolrManager solrManager;
  private ViewerDatabase database;
  private String databaseUUID;
  private String jobUUID;
  private Map<DenormalizeConfiguration, List<Tree<RelatedTablesConfiguration>>> structure = new HashMap<>();
  private DataTransformationObserver observer;

  public Denormalize(String databaseUUID, String tableUUID, String jobUUID) throws ModuleException {
    this.solrManager = ViewerFactory.getSolrManager();
    this.databaseUUID = databaseUUID;
    this.jobUUID = jobUUID;
    observer = new DataTransformationObserver(databaseUUID, tableUUID);
    try {
      database = solrManager.retrieve(ViewerDatabase.class, this.databaseUUID);
      DenormalizeConfiguration denormalizeConfiguration = getConfiguration(
        Paths.get(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + tableUUID + ViewerConstants.JSON_EXTENSION),
        DenormalizeConfiguration.class);

      cleanNestedDocuments(denormalizeConfiguration);
      buildDenormalizeTree(denormalizeConfiguration);

      if (structure.isEmpty()) {
        return;
      }
      denormalize();
      updateCollectionStatus(denormalizeConfiguration);

    } catch (NotFoundException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }
  }

  private void updateCollectionStatus(DenormalizeConfiguration denormalizeConfiguration) throws GenericException {
    for (RelatedTablesConfiguration relatedTable : denormalizeConfiguration.getRelatedTables()) {
      if(relatedTable.getColumnsIncluded().isEmpty()) continue;
      ViewerColumn viewerColumn = new ViewerColumn();
      viewerColumn.setDescription("Please EDIT");
      viewerColumn.setSolrName(relatedTable.getTableID());
      List<String> columnsId = new ArrayList<>();
      List<String> columnName = new ArrayList<>();

      for (RelatedColumnConfiguration column : relatedTable.getColumnsIncluded()) {
        columnsId.add(column.getSolrName());
        columnName.add(column.getColumnName());
      }
      viewerColumn.setDisplayName(columnName.toString());
      ViewerFactory.getConfigurationManager().addDenormalizationColumns(databaseUUID,
          denormalizeConfiguration.getTableUUID(), viewerColumn, columnsId);
    }
  }

  /**
   * @param path
   * @param objectClass
   * @param <T>
   * @return
   * @throws ModuleException
   */
  private <T> T getConfiguration(Path path, Class<T> objectClass) throws ModuleException {
    Path configurationPath = ViewerConfiguration.getInstance().getDatabasesPath().resolve(database.getUuid())
      .resolve(path);
    if (Files.exists(configurationPath)) {
      return JsonTransformer.readObjectFromFile(configurationPath, objectClass);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist: " + configurationPath.toString());
    }
  }

  private void buildDenormalizeTree(DenormalizeConfiguration denormalizeConfig) {
    List<Tree<RelatedTablesConfiguration>> rootList = new ArrayList<>();
    for (RelatedTablesConfiguration relatedTable : denormalizeConfig.getRelatedTables()) {
      if (relatedTable.getReferencedTableUUID().equals(denormalizeConfig.getTableUUID())) {
        Tree<RelatedTablesConfiguration> root = new Tree<>(relatedTable);
        setupChildren(root, relatedTable, denormalizeConfig);
        rootList.add(root);
      }
    }
    structure.put(denormalizeConfig, rootList);
  }

  private void setupChildren(Tree<RelatedTablesConfiguration> root, RelatedTablesConfiguration parentTable,
    DenormalizeConfiguration denormalizeConfiguration) {
    for (RelatedTablesConfiguration relatedTable : denormalizeConfiguration.getRelatedTables()) {
      if (relatedTable.getReferencedTableUUID().equals(parentTable.getTableUUID())) {
        Tree<RelatedTablesConfiguration> child = root.addChild(relatedTable);
        setupChildren(child, relatedTable, denormalizeConfiguration);
      }
    }
  }

  private void denormalize() throws ModuleException {
    for (Map.Entry<DenormalizeConfiguration, List<Tree<RelatedTablesConfiguration>>> entry : structure.entrySet()) {
      DenormalizeConfiguration root = entry.getKey();
      List<Tree<RelatedTablesConfiguration>> treeList = entry.getValue();
      SolrQuery rootQuery = buildRootQuery(root, treeList);
      List<SolrQuery> queryList = new ArrayList<>();
      for (Tree<RelatedTablesConfiguration> child : treeList) {
        List<List<RelatedTablesConfiguration>> pathList = new ArrayList<>();
        convertTreePathInList(child, pathList);

        for (List<RelatedTablesConfiguration> paths : pathList) {
          List<SolrQuery> bundleQuery = new ArrayList<>();
          bundleQuery.add(rootQuery);
          bundleQuery.addAll(buildChildQuery(paths));
          SolrQuery query = createdNestedQuery(bundleQuery);
          queryList.add(query);
          System.out.println(query);
          System.out.println("--------------------------------------------------------------------------");
        }
      }

      try {
        long processedRows = 0;
        for (SolrQuery query : queryList) {
          IterableNestedIndexResult sourceRows = solrManager.findAllRows(databaseUUID, query, null);
          Long rowToProcess = sourceRows.getTotalCount() * queryList.size();
          solrManager.editBatchJob(jobUUID, rowToProcess, processedRows);
          for (ViewerRow row : sourceRows) {
            solrManager.editBatchJob(jobUUID, rowToProcess, ++processedRows);
            List<SolrInputDocument> nestedDocument = new ArrayList<>();
            createdNestedDocument(row, nestedDocument);
            if (!nestedDocument.isEmpty()) {
              solrManager.addDatabaseField(databaseUUID, row.getUuid(), nestedDocument);
            }
          }
        }
      } catch (NotFoundException | GenericException e) {
        throw new ModuleException().withMessage("Cannot update batch row in solr");
      }
    }
  }

  private void convertTreePathInList(Tree<RelatedTablesConfiguration> root,
    List<List<RelatedTablesConfiguration>> pathList) {
    if (root.getChildren() == null || root.getChildren().isEmpty()) {
      List<RelatedTablesConfiguration> list = new ArrayList<>();
      buildPath(root, list);
      pathList.add(list);
    }
    for (Tree<RelatedTablesConfiguration> child : root.getChildren()) {
      convertTreePathInList(child, pathList);
    }
  }

  private void buildPath(Tree<RelatedTablesConfiguration> node, List<RelatedTablesConfiguration> list) {
    if (node.getParent() != null) {
      buildPath(node.getParent(), list);
    }
    list.add(node.getValue());
  }

  private void cleanNestedDocuments(DenormalizeConfiguration root) {
    Filter filter = FilterUtils.filterByTable(new Filter(), root.getTableID());

    IterableIndexResult allRows = solrManager.findAllRows(databaseUUID, filter, null, new ArrayList<>());
    for (ViewerRow row : allRows) {
      solrManager.deleteNestedDocuments(databaseUUID, row.getUuid());
    }

    for (RelatedTablesConfiguration relatedTable : root.getRelatedTables()) {
      solrManager.deleteNestedDocuments(databaseUUID, relatedTable.getUuid());
    }
  }

  private SolrQuery buildRootQuery(DenormalizeConfiguration root, List<Tree<RelatedTablesConfiguration>> treeList)
    throws ModuleException {
    Filter filter = FilterUtils.filterByTable(new Filter(), root.getTableID());
    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    for (Tree<RelatedTablesConfiguration> tree : treeList) {
      for (ReferencesConfiguration reference : tree.getValue().getReferences()) {
        fieldsToReturn.add(String.format("aux_%1$s:%1$s", reference.getReferencedTable().getSolrName()));
      }
    }

    fieldsToReturn.add("nested:[subquery]");

    try {
      return SolrUtils.buildQuery(filter, fieldsToReturn);
    } catch (RequestNotValidException e) {
      throw new ModuleException().withMessage("Cannot retrieved row from solr");
    }
  }

  private List<SolrQuery> buildChildQuery(List<RelatedTablesConfiguration> paths) throws ModuleException {
    List<SolrQuery> queryList = new ArrayList<>();
    for (int i = 0; i < paths.size(); i++) {
      RelatedTablesConfiguration relatedTable = paths.get(i);
      Filter resultingFilter = new Filter();
      List<String> fieldsToReturn = new ArrayList<>();
      List<FilterParameter> filterParameterList = new ArrayList<>();

      // tableId:{sakila.film_actor}
      filterParameterList.add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, relatedTable.getTableID()));

      for (ReferencesConfiguration reference : relatedTable.getReferences()) {
        RelatedColumnConfiguration sourceTable = reference.getSourceTable();
        RelatedColumnConfiguration referencedTable = reference.getReferencedTable();

        // eg: {!terms f=col0_l v=$row.col4_l}
        filterParameterList.add(
          new TermsFilterParameter(sourceTable.getSolrName(), String.format("$row.%s", referencedTable.getSolrName())));
      }

      // (tableId:{sakila.film_actor} AND {!terms f=col0_l v=$row.col4_l})
      resultingFilter.add(new AndFiltersParameters(filterParameterList));

      fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);
      fieldsToReturn.add(ViewerConstants.INDEX_ID);
      fieldsToReturn
        .add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, ViewerConstants.INDEX_ID));
      fieldsToReturn
        .add(String.format("%s:%s", ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, ViewerConstants.SOLR_ROWS_TABLE_ID));
      fieldsToReturn.add(String.format("%s:\"%s\"", ViewerConstants.SOLR_ROWS_NESTED_UUID, relatedTable.getUuid()));
      for (RelatedColumnConfiguration columnConfiguration : relatedTable.getColumnsIncluded()) {
        fieldsToReturn.add(columnConfiguration.getSolrName());
      }

      if (paths.size() > i + 1 && paths.get(i + 1) != null) {
        for (ReferencesConfiguration reference : paths.get(i + 1).getReferences()) {
          fieldsToReturn.add(String.format("aux_%1$s:%1$s", reference.getReferencedTable().getSolrName()));
        }
        fieldsToReturn.add("nested:[subquery]");
      } else {
        // TODO if is the last one, check if has column to include
      }

      try {
        queryList.add(SolrUtils.buildQuery(resultingFilter, fieldsToReturn));
      } catch (RequestNotValidException e) {
        throw new ModuleException().withMessage("Cannot retrieved row from solr");
      }
    }
    return queryList;
  }

  public SolrQuery createdNestedQuery(List<SolrQuery> queryList) {
    SolrQuery query = new SolrQuery();
    String nestedPath = "";
    for (int i = 0; i < queryList.size(); i++) {
      SolrQuery subquery = queryList.get(i);
      if (i == 0) {
        query.set("q", subquery.getQuery());
        if (subquery.getFields() != null && !subquery.getFields().isEmpty()) {
          query.set("fl", subquery.getFields());
        }
      } else {
        if (nestedPath.isEmpty()) {
          nestedPath = "nested";
        } else {
          nestedPath = nestedPath + ".nested";
        }
        query.set(nestedPath + ".q", subquery.getQuery());
        query.set(nestedPath + ".rows", 100);
        if (subquery.getFields() != null && !subquery.getFields().isEmpty()) {
          query.set(nestedPath + ".fl", subquery.getFields());
        }
      }
    }

    return query;
  }

  private void createdNestedDocument(ViewerRow row, List<SolrInputDocument> nestedDocument) {
    if (row.getNestedRowList() != null) {
      for (ViewerRow document : row.getNestedRowList()) {
        Map<String, ViewerCell> cells = document.getCells();
        String uuid = row.getUuid() + ViewerConstants.API_SEP + document.getUuid();

        Map<String, Object> fields = new HashMap<>();
        for (Map.Entry<String, ViewerCell> cell : cells.entrySet()) {
          String key = cell.getKey();

          ViewerCell cellValue = cell.getValue();
          if (key.endsWith(ViewerConstants.SOLR_DYN_DATE)) {
            fields.put(key, JodaUtils.xsDateParse(cellValue.getValue()).toString());
          } else {
            fields.put(key, cellValue.getValue());
          }
        }
        if (!fields.isEmpty()) {
          nestedDocument.add(solrManager.createNestedDocument(uuid, row.getUuid(), document.getNestedOriginalUUID(),
            fields, document.getTableId(), document.getNestedUUID()));
        }

        if (document.getNestedRowList() == null) {
          continue;
        } else {
          createdNestedDocument(document, nestedDocument);
        }
      }
    }
  }
}
