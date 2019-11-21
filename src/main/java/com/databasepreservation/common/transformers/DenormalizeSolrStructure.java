package com.databasepreservation.common.transformers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.databasepreservation.common.client.models.denormalize.*;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.filter.solr.TermsFilterParameter;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
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
  private DenormalizeListConfiguration configuration;

  public DenormalizeSolrStructure(String databaseUUID, DenormalizeListConfiguration configuration)
    throws ModuleException {
    solrManager = ViewerFactory.getSolrManager();
    this.databaseUUID = databaseUUID;
    this.configuration = configuration;
    try {
      database = solrManager.retrieve(ViewerDatabase.class, this.databaseUUID);
    } catch (NotFoundException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }
  }

  public void denormalize() throws ModuleException {
    List<DenormalizeConfiguration> denormalizeList = configuration.getDenormalizeList();
    for (DenormalizeConfiguration item : denormalizeList) {
      List<ReferencedConfiguration> allRootReferencedColumn = item.getAllRootReferencedColumn(item.getTableUUID());
      getRowsToDenormalize(item, allRootReferencedColumn);
    }
    System.out.println("denormalize() ended");
  }

  private void getRowsToDenormalize(DenormalizeConfiguration item,
    List<ReferencedConfiguration> allRootReferencedColumn) throws ModuleException {
    Filter filter = FilterUtils.filterByTable(new Filter(), item.getTableUUID());
    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    fieldsToReturn.add(ViewerConstants.SOLR_ROWS_TABLE_ID);

    for(ReferencedConfiguration referenced : allRootReferencedColumn){
      fieldsToReturn.add(referenced.getSolrName());
    }

    IterableIndexResult sourceRows = solrManager.findAllRows(databaseUUID, filter, null, fieldsToReturn);
    for (ViewerRow row : sourceRows) {
      buildMainQuery(item, row);
    }
  }

  private void buildMainQuery(DenormalizeConfiguration item, ViewerRow row) throws ModuleException {
    Map<String, ViewerCell> cells = row.getCells();
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();
    List<SolrQuery> queryList = new ArrayList<>();
    for(Map.Entry<String, ViewerCell> entry : cells.entrySet()){
      filterParameterList.add(new AndFiltersParameters(
          Arrays.asList(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, item.getTableUUID()),
              new SimpleFilterParameter(entry.getKey(), entry.getValue().getValue()))));
    }
    resultingFilter.add(filterParameterList);

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add("*");
    if(item.checkIfTableIsReferenced(item.getTableUUID())){
      fieldsToReturn.add("nested:[subquery]");
    }
    try {
      SolrQuery entries = SolrUtils.buildQuery(0, resultingFilter, fieldsToReturn);
      queryList.add(entries);
      Map<String, List<String>> fieldsToDisplay = new HashMap<>();
      for(ColumnsToIncludeConfiguration table : item.getAllTables()) {
        queryList.add(buildSubQuery(item, table));
        if(fieldsToDisplay.get(table.getTableUUID()) == null ){
          fieldsToDisplay.put(table.getTableUUID(), new ArrayList<>());
        }
        fieldsToDisplay.get(table.getTableUUID()).addAll(buildColumnsToDisplay(table));
      }

      ViewerRow document = solrManager.findRows(databaseUUID, queryList).getResults().get(0);

      List<SolrInputDocument> nestedDocument = new ArrayList<>();

      if (document.getNestedRowList() != null) {
        buildNestedDocumentList(document.getNestedRowList(), nestedDocument, document.getUuid(),
          document.getTableId(), fieldsToDisplay);
      }

      if (!nestedDocument.isEmpty()) {
        solrManager.addDatabaseField(databaseUUID, document.getUuid(), nestedDocument);
      }

    } catch (RequestNotValidException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved row from solr");
    }
  }

  private void buildNestedDocumentList(List<ViewerRow> documentList, List<SolrInputDocument> nestedDocument,
    String uuid, String referenceTableId, Map<String, List<String>> fieldsToDisplay) {
    for (ViewerRow document : documentList) {
      Map<String, ViewerCell> cells = document.getCells();
      String nestedUUID = uuid + "." + document.getUuid();

      Map<String, Object> fields = new HashMap<>();
      for (Map.Entry<String, ViewerCell> cell : cells.entrySet()) {
        String key = cell.getKey();
        if(fieldsToDisplay.get(document.getTableId()) != null && !fieldsToDisplay.get(document.getTableId()).contains(key)){
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
          .add(solrManager.createNestedDocument(nestedUUID, fields, referenceTableId, document.getTableId()));
      }

      if (document.getNestedRowList() == null) {
        return;
      } else {
        buildNestedDocumentList(document.getNestedRowList(), nestedDocument, nestedUUID, document.getTableId(), fieldsToDisplay);
      }
    }
  }

  private SolrQuery buildSubQuery(DenormalizeConfiguration item, ColumnsToIncludeConfiguration table)
    throws RequestNotValidException {
    Filter resultingFilter = new Filter();
    List<FilterParameter> filterParameterList = new ArrayList<>();

    ReferencedConfiguration referenced = table.getForeignKey().getReferenced();
    ReferenceConfiguration reference = table.getForeignKey().getReference();

    filterParameterList.add(new AndFiltersParameters(
        Arrays.asList(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, table.getTableUUID()),
        new TermsFilterParameter(reference.getSolrName(), "$row." + referenced.getSolrName()))));

    resultingFilter.add(filterParameterList);

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add("*");
    if(item.checkIfTableIsReferenced(table.getTableUUID())){
      fieldsToReturn.add("nested:[subquery]");
    }

    return SolrUtils.buildQuery(item.checkNestedLevel(table.getTableUUID(), 0), resultingFilter, fieldsToReturn);
  }

  private static List<String> buildColumnsToDisplay(ColumnsToIncludeConfiguration item) {
    List<String> displayColumns = new ArrayList<>();
    String displayFormat = item.getDisplayFormat();

    Pattern pattern = Pattern.compile("col[0-9]*_\\w");
    Matcher matcher = pattern.matcher(displayFormat);
    while (matcher.find()) {
      displayColumns.add(matcher.group());
    }

    return displayColumns;
  }
}
