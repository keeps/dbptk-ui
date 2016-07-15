package com.databasepreservation.visualization.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchField;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BrowserServiceUtils {
  public static List<SearchField> getSearchFieldsFromTable(ViewerTable viewerTable) {
    List<SearchField> searchFields = new ArrayList<>();

    for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
      SearchField searchField = new SearchField(viewerTable.getUUID() + "-"
        + viewerColumn.getColumnIndexInEnclosingTable(), Arrays.asList(viewerColumn.getSolrName()),
        viewerColumn.getDisplayName(), RodaConstants.SEARCH_FIELD_TYPE_TEXT);
      searchField.setFixed(true);
      // fixme: use ViewerType.dbTypes instead of treating everything as text

      searchFields.add(searchField);
    }

    return searchFields;
  }
}
