package com.databasepreservation.visualization.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.databasepreservation.visualization.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerType;
import com.databasepreservation.visualization.client.common.search.SearchField;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BrowserServiceUtils {
  public static List<SearchField> getSearchFieldsFromTable(ViewerTable viewerTable) {
    List<SearchField> searchFields = new ArrayList<>();

    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      SearchField searchField = new SearchField(
        viewerTable.getUUID() + "-" + viewerColumn.getColumnIndexInEnclosingTable(),
        Arrays.asList(viewerColumn.getSolrName()), viewerColumn.getDisplayName(),
        viewerTypeToSearchFieldType(viewerColumn.getType()));
      searchField.setFixed(true);
      searchFields.add(searchField);
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
      case BINARY:
      case COMPOSED_STRUCTURE:
      case COMPOSED_ARRAY:
      default:
        return "unsupported";
    }
  }
}
