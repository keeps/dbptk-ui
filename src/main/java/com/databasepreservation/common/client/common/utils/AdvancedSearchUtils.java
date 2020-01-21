package com.databasepreservation.common.client.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class AdvancedSearchUtils {
  public static List<SearchField> getSearchFieldsForLog() {
    ClientMessages messages = GWT.create(ClientMessages.class);
    List<SearchField> searchFields = new ArrayList<>();

    SearchField address = new SearchField(ViewerConstants.SOLR_ACTIVITY_LOG_IP_ADDRESS,
      Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_IP_ADDRESS),
      messages.activityLogTextForAddress(), ViewerConstants.SEARCH_FIELD_TYPE_TEXT);
    address.setFixed(true);

    SearchField date = new SearchField(ViewerConstants.SOLR_ACTIVITY_LOG_DATETIME,
      Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_DATETIME), messages.activityLogTextForDate(),
      ViewerConstants.SEARCH_FIELD_TYPE_DATETIME);
    date.setFixed(true);

    searchFields.add(address);
    searchFields.add(date);

    return searchFields;
  }

  public static List<SearchField> getSearchFieldsFromTable(ViewerTable viewerTable,  CollectionStatus status) {
    return getSearchFieldsFromTable(viewerTable, status, null);
  }

  public static List<SearchField> getSearchFieldsFromTable(ViewerTable viewerTable, CollectionStatus status, ViewerMetadata metadata) {
    List<SearchField> searchFields = new ArrayList<>();

    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      SearchField searchField = new SearchField(
        viewerTable.getUuid() + "-" + viewerColumn.getColumnIndexInEnclosingTable(),
        Collections.singletonList(viewerColumn.getSolrName()), viewerColumn.getDisplayName(),
        viewerTypeToSearchFieldType(viewerColumn.getType()));
      searchField.setFixed(status.showAdvancedSearch(viewerTable.getUuid(), viewerColumn.getSolrName()));
      searchFields.add(searchField);
    }

    // Add nested columns
    for (ColumnStatus columnStatus : status.getTableStatus(viewerTable.getUuid()).getColumns()) {
      NestedColumnStatus nestedColumns = columnStatus.getNestedColumns();
      if(nestedColumns != null) {
        for (String nestedColumn : nestedColumns.getNestedFields()) {
          ViewerTable nestedTable = metadata.getTableById(nestedColumns.getOriginalTable());
          for (ViewerColumn column : nestedTable.getColumns()) {
            if(column.getDisplayName().equals(nestedColumn)){
              ViewerType nestedType = new ViewerType();
              nestedType.setDbType(ViewerType.dbTypes.NESTED);
              List<String> fields = new ArrayList<>();
              fields.add(column.getSolrName());
              fields.add(viewerTable.getId());
              fields.add(nestedTable.getId());
              SearchField searchField = new SearchField(
                  columnStatus.getId() + "-" + column.getColumnIndexInEnclosingTable(),
                  fields, nestedTable.getName() + ":" + column.getDisplayName(),
                  viewerTypeToSearchFieldType(nestedType));

              searchField.setFixed(status.showAdvancedSearch(viewerTable.getUuid(), columnStatus.getId()));
              searchFields.add(searchField);
            }
          }
        }
      }
    }

    return searchFields;
  }

  private static String viewerTypeToSearchFieldType(ViewerType viewerType) {
    ViewerType.dbTypes dbType = viewerType.getDbType();

    switch (dbType) {

      case BOOLEAN:
        return ViewerConstants.SEARCH_FIELD_TYPE_BOOLEAN;
      case DATETIME:
        return ViewerConstants.SEARCH_FIELD_TYPE_DATETIME;
      case DATETIME_JUST_DATE:
        return ViewerConstants.SEARCH_FIELD_TYPE_DATE;
      case DATETIME_JUST_TIME:
        return ViewerConstants.SEARCH_FIELD_TYPE_TIME;
      case TIME_INTERVAL:
        return ViewerConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL;
      case NUMERIC_FLOATING_POINT:
      case NUMERIC_INTEGER:
        return ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL;
      case ENUMERATION:
      case STRING:
        return ViewerConstants.SEARCH_FIELD_TYPE_TEXT;
      case NESTED:
        return ViewerConstants.SEARCH_FIELD_TYPE_NESTED;
      case BINARY:
      case COMPOSED_STRUCTURE:
      case COMPOSED_ARRAY:
      default:
        return "unsupported";
    }
  }
}