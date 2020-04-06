package com.databasepreservation.common.client.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerNestedColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
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
      Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_IP_ADDRESS), messages.activityLogTextForAddress(),
      ViewerConstants.SEARCH_FIELD_TYPE_TEXT);
    address.setFixed(true);

    SearchField date = new SearchField(ViewerConstants.SOLR_ACTIVITY_LOG_DATETIME,
      Collections.singletonList(ViewerConstants.SOLR_ACTIVITY_LOG_DATETIME), messages.activityLogTextForDate(),
      ViewerConstants.SEARCH_FIELD_TYPE_DATETIME);
    date.setFixed(true);

    searchFields.add(address);
    searchFields.add(date);

    return searchFields;
  }

  public static List<SearchField> getSearchFieldsFromTable(ViewerTable viewerTable, ViewerCollectionConfiguration status) {
    return getSearchFieldsFromTable(viewerTable, status, null);
  }

  public static Map<String, List<SearchField>> getSearchFieldsFromTableMap(ViewerTable viewerTable,
    ViewerCollectionConfiguration status) {
    Map<String, List<SearchField>> map = new LinkedHashMap<>();
    final ViewerTableConfiguration configTable = status.getViewerTableConfigurationByTableId(viewerTable.getId());

    for (ViewerColumnConfiguration column : configTable.getColumns()) {
      if (!column.getType().equals(ViewerType.dbTypes.NESTED)) {
        if (column.getViewerSearchConfiguration().getAdvanced().isFixed()) {
          SearchField searchField = new SearchField(configTable.getId() + "-" + column.getColumnIndex(),
            Collections.singletonList(column.getId()), column.getCustomName(),
            viewerTypeToSearchFieldType(column.getType()));
          searchField.setFixed(column.getViewerSearchConfiguration().getAdvanced().isFixed());
          updateSearchFieldMap(map, configTable.getCustomName(), searchField);
        }
      } else {
        ViewerNestedColumnConfiguration nestedColumns = column.getNestedColumns();
        if (nestedColumns != null) {
          for (String columnSolrName : nestedColumns.getNestedSolrNames()) {
            ViewerTableConfiguration configNestedTable = status.getViewerTableConfigurationByTableId(nestedColumns.getOriginalTable());
            for (ViewerColumnConfiguration nestedColumn : configNestedTable.getColumns()) {
              if (nestedColumn.getId().equals(columnSolrName) && column.getViewerSearchConfiguration().getAdvanced().isFixed()) {
                ViewerType nestedType = new ViewerType();
                nestedType.setDbType(ViewerType.dbTypes.NESTED);
                List<String> fields = new ArrayList<>();
                fields.add(columnSolrName);
                fields.add(configTable.getId());
                fields.add(column.getId());
                SearchField searchField = new SearchField(column.getId() + "-" + column.getColumnIndex(),
                    fields, nestedColumn.getCustomName(), viewerTypeToSearchFieldType(nestedType));
                searchField.setFixed(status.showAdvancedSearch(viewerTable.getUuid(), column.getId()));
                updateSearchFieldMap(map, column.getCustomName(), searchField);
              }
            }
          }
        }
      }
    }

    return map;
  }

  public static List<SearchField> getSearchFieldsFromTable(ViewerTable viewerTable, ViewerCollectionConfiguration status,
    ViewerMetadata metadata) {
    List<SearchField> searchFields = new ArrayList<>();

    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      SearchField searchField = new SearchField(
        viewerTable.getUuid() + "-" + viewerColumn.getColumnIndexInEnclosingTable(),
        Collections.singletonList(viewerColumn.getSolrName()),
        status.getViewerTableConfigurationByTableId(viewerTable.getId()).getColumnById(viewerColumn.getSolrName()).getCustomName(),
        viewerTypeToSearchFieldType(viewerColumn.getType()));
      searchField.setFixed(status.showAdvancedSearch(viewerTable.getUuid(), viewerColumn.getSolrName()));
      searchFields.add(searchField);
    }

    // Add nested columns
    for (ViewerColumnConfiguration viewerColumnConfiguration : status.getViewerTableConfiguration(viewerTable.getUuid()).getColumns()) {
      ViewerNestedColumnConfiguration nestedColumns = viewerColumnConfiguration.getNestedColumns();
      if (nestedColumns != null) {
        for (String nestedColumn : nestedColumns.getNestedFields()) {
          ViewerTable nestedTable = metadata.getTableById(nestedColumns.getOriginalTable());
          for (ViewerColumn column : nestedTable.getColumns()) {
            if (column.getDisplayName().equals(nestedColumn)) {
              ViewerType nestedType = new ViewerType();
              nestedType.setDbType(ViewerType.dbTypes.NESTED);
              List<String> fields = new ArrayList<>();
              fields.add(column.getSolrName());
              fields.add(viewerTable.getId());
              fields.add(nestedTable.getId());
              SearchField searchField = new SearchField(
                viewerColumnConfiguration.getId() + "-" + column.getColumnIndexInEnclosingTable(), fields,
                nestedTable.getName() + ":" + column.getDisplayName(), viewerTypeToSearchFieldType(nestedType));

              searchField.setFixed(status.showAdvancedSearch(viewerTable.getUuid(), viewerColumnConfiguration.getId()));
              searchFields.add(searchField);
            }
          }
        }
      }
    }

    return searchFields;
  }

  private static void updateSearchFieldMap(Map<String, List<SearchField>> map, String table, SearchField searchField) {
    if (map.get(table) != null) {
      final List<SearchField> searchFields = map.get(table);
      searchFields.add(searchField);
    } else {
      List<SearchField> searchFields = new ArrayList<>();
      searchFields.add(searchField);
      map.put(table, searchFields);
    }
  }

  private static String viewerTypeToSearchFieldType(ViewerType.dbTypes type) {
    switch (type) {
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

  private static String viewerTypeToSearchFieldType(ViewerType viewerType) {
    return viewerTypeToSearchFieldType(viewerType.getDbType());
  }
}
