/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.activity.log.operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FilterOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    try {
      ActivityLogEntry log = wrapper.getActivityLogEntry();
      if (wrapper.getDatabasePresence().equals(PresenceState.YES)) {
        wrapper.setFilter(replaceColumnSolrName(wrapper.getDatabase().getMetadata(), log.getParameters()));
        wrapper.setFilterPresence(PresenceState.YES);
      } else {
        final String jsonFilter = log.getParameters().get(ViewerConstants.CONTROLLER_FILTER_PARAM);
        if (jsonFilter == null) {
          return wrapper;
        }
        final Filter filter = JsonUtils.getObjectFromJson(jsonFilter, Filter.class);
        wrapper.setFilterPresence(PresenceState.YES);
        wrapper.setFilter(filter);
      }
    } catch (GenericException e) {
      LOGGER.debug("Error executing the retrieve filter information", e);
    }

    return wrapper;
  }

  private Filter replaceColumnSolrName(ViewerMetadata metadata, Map<String, String> parameters)
    throws GenericException {

    final String jsonFilter = parameters.get(ViewerConstants.CONTROLLER_FILTER_PARAM);
    final Filter filter = JsonUtils.getObjectFromJson(jsonFilter, Filter.class);

    final Map<String, String> mapperSolrToDisplayName = getDisplayNameColumn(metadata, parameters, filter);

    for (FilterParameter filterParameter : filter.getParameters()) {
      if (isColumnFilterParameter(filterParameter)) {
        filterParameter.setName(mapperSolrToDisplayName.get(filterParameter.getName()));
      }
    }

    return filter;
  }

  // check if filter parameter is not scoped to a column
  // ex EDismaxSimplerQueryFilterParameter type filters
  private boolean isColumnFilterParameter(FilterParameter filterParameter) {
    final String name = filterParameter.getName();
    return name != null && name.startsWith(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX);
  }

  private String getTableIdFromFilter(Filter filter) {
    for (FilterParameter filterParameter : filter.getParameters()) {
      if (filterParameter instanceof SimpleFilterParameter
        && ViewerConstants.SOLR_ROWS_TABLE_ID.equals(filterParameter.getName())) {
        return ((SimpleFilterParameter) filterParameter).getValue();
      }
    }
    return null;
  }

  private ViewerTable getTable(ViewerMetadata metadata, Map<String, String> parameters, Filter filter) {
    String tableId = parameters.get(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);
    if (tableId == null) {
      tableId = getTableIdFromFilter(filter);
    }

    if (tableId == null) {
      LOGGER.debug("Unable to determine the table for this activity log entry, "
        + "Solr column names will not be replaced by their display names");
      return null;
    }

    return metadata.getTableById(tableId);
  }

  private Map<String, String> getDisplayNameColumn(ViewerMetadata metadata, Map<String, String> parameters,
    Filter filter) {
    Map<String, String> solrNameToDisplayName = new HashMap<>();

    final ViewerTable table = getTable(metadata, parameters, filter);
    if (table == null) {
      return solrNameToDisplayName;
    }

    for (FilterParameter filterParameter : filter.getParameters()) {
      if (isColumnFilterParameter(filterParameter)) {
        final List<ViewerColumn> columns = table.getColumns();
        for (ViewerColumn column : columns) {
          if (column.getSolrName().equals(filterParameter.getName())) {
            solrNameToDisplayName.put(column.getSolrName(), column.getDisplayName());
          }
        }
      }
    }
    return solrNameToDisplayName;
  }
}
