package com.databasepreservation.common.transformers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.server.index.utils.SolrUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.zookeeper.server.admin.JsonOutputter;
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
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.filter.solr.TermsFilterParameter;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeStructure {
  private final DatabaseRowsSolrManager solrManager;
  private ViewerDatabase database;
  private String databaseUUID;
  private CollectionConfiguration configuration;

  /**
   *
   * @param databaseUUID
   * @throws ModuleException
   */
  public DenormalizeStructure(String databaseUUID) throws ModuleException {
    this.solrManager = ViewerFactory.getSolrManager();
    this.databaseUUID = databaseUUID;
    try {
      database = solrManager.retrieve(ViewerDatabase.class, this.databaseUUID);
      this.configuration = getConfiguration(Paths.get(databaseUUID + ViewerConstants.JSON_EXTENSION),
        CollectionConfiguration.class);
      denormalize();
    } catch (NotFoundException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }
  }

  /**
   *
   * @param path
   * @param objectClass
   * @param <T>
   * @return
   * @throws ModuleException
   */
  private <T> T getConfiguration(Path path, Class<T> objectClass) throws ModuleException {
    Path configurationPath = ViewerConfiguration.getInstance().getDatabaseConfigPath().resolve(database.getUuid())
      .resolve(path);
    if (Files.exists(configurationPath)) {
      return JsonTransformer.readObjectFromFile(configurationPath, objectClass);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist");
    }
  }

  /**
   *
   * @throws ModuleException
   */
  private void denormalize() throws ModuleException {
    for (TableConfiguration tableConfig : configuration.getTables()) {
      DenormalizeConfiguration denormalizeConfig = getConfiguration(
        Paths.get(tableConfig.getUuid() + "-CURRENT" + ViewerConstants.JSON_EXTENSION), DenormalizeConfiguration.class);
      getRowsToDenormalize(denormalizeConfig);
    }
  }

  /**
   *
   * @param denormalizeConfig
   */
  private void getRowsToDenormalize(DenormalizeConfiguration denormalizeConfig) throws ModuleException {
    Filter filter = FilterUtils.filterByTable(new Filter(), denormalizeConfig.getTableID());
    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    for (RelatedTablesConfiguration relatedTable : denormalizeConfig.getRelatedTables()) {
      if (relatedTable.getReferencedTableUUID().equals(denormalizeConfig.getTableUUID())) {
        for (ReferencesConfiguration references : relatedTable.getReferences()) {
          fieldsToReturn.add(references.getReferencedTable().getSolrName());
        }
      }
    }

    IterableIndexResult sourceRows = solrManager.findAllRows(databaseUUID, filter, null, fieldsToReturn);
    for (ViewerRow row : sourceRows) {
      for (RelatedTablesConfiguration relatedTable : denormalizeConfig.getRelatedTables()) {
        if (relatedTable.getReferencedTableUUID().equals(denormalizeConfig.getTableUUID())) {
          System.out.println();
          System.out.println("===============================================================");
          System.out.println("> Related Table : " + relatedTable.getTableID());
          System.out.println("***************************************************************");
          buildMainQuery(denormalizeConfig, row, relatedTable);
        }
      }
      // TODO: remove
      break;
    }
  }

  private void buildMainQuery(DenormalizeConfiguration denormalizeConfig, ViewerRow row,
    RelatedTablesConfiguration relatedTable) throws ModuleException {
    Map<String, ViewerCell> cells = row.getCells();
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();
    List<String> fieldsToReturn = new ArrayList<>();
    List<SolrQuery> queryList = new ArrayList<>();

    // tableId:{referenceTableID}
    filterParameterList
      .add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, relatedTable.getReferencedTableID()));

    for (ReferencesConfiguration reference : relatedTable.getReferences()) {
      if (cells.get(reference.getReferencedTable().getSolrName()) == null) {
        System.out.println("----------------------------SKIP------------------------------");
        System.out.println("===============================================================");
        return;
      }

      // {colSolrName}:{value}
      filterParameterList.add(new SimpleFilterParameter(reference.getReferencedTable().getSolrName(),
        cells.get(reference.getReferencedTable().getSolrName()).getValue()));

      // fl={colSolrName},..
      fieldsToReturn.add(reference.getReferencedTable().getSolrName());
    }
    // (tableId:{referenceTableID} AND {colSolrName}:{value})
    resultingFilter.add(new AndFiltersParameters(filterParameterList));

    try {
      queryList.add(SolrUtils.buildQuery(resultingFilter, fieldsToReturn));
    } catch (RequestNotValidException e) {
      throw new ModuleException().withMessage("Cannot retrieved row from solr");
    }
    for (RelatedTablesConfiguration tableConfig : denormalizeConfig.getRelatedTables()) {
      if (tableConfig.getReferencedTableUUID().equals(relatedTable.getTableUUID())) {
        fieldsToReturn.add("nested:[subquery]");
        queryList.add(buildSubQuery(denormalizeConfig, tableConfig));
        break;
      }
    }
    System.out.println("===============================================================");
    for (SolrQuery entries : queryList) {
      System.out.println(entries);
    }

    System.out.println("===============================================================");
  }

  private SolrQuery buildSubQuery(DenormalizeConfiguration denormalizeConfig, RelatedTablesConfiguration relatedTable) throws ModuleException {
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();
    List<String> fieldsToReturn = new ArrayList<>();

    // tableId:{referenceTableID}
    filterParameterList
      .add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, relatedTable.getReferencedTableID()));

    for (ReferencesConfiguration reference : relatedTable.getReferences()) {
      RelatedColumnConfiguration sourceTable = reference.getSourceTable();
      RelatedColumnConfiguration referencedTable = reference.getReferencedTable();

      // eg: {!terms f=col0_l v=$row.col4_l}
      filterParameterList
        .add(new TermsFilterParameter(sourceTable.getSolrName(), "$row." + referencedTable.getSolrName()));
    }

    // (tableId:{referenceTableID} AND {!terms f=col0_l v=$row.col4_l})
    resultingFilter.add(new AndFiltersParameters(filterParameterList));

    for (RelatedTablesConfiguration tableConfig : denormalizeConfig.getRelatedTables()) {
      if (tableConfig.getReferencedTableUUID().equals(relatedTable.getTableUUID())) {
        fieldsToReturn.add("nested:[subquery]");
        buildSubQuery(denormalizeConfig, tableConfig);
        break;
      }
    }

    try {
      return SolrUtils.buildQuery(resultingFilter, fieldsToReturn);
    } catch (RequestNotValidException e) {
      throw new ModuleException().withMessage("Cannot retrieved row from solr");
    }
  }
}
