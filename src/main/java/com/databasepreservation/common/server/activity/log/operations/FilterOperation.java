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

import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;

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

    final Map<String, String> mapperSolrToDisplayName = getDisplayNameColumn(metadata, filter);

    for (FilterParameter filterParameter : filter.getParameters()) {
      if (filterParameter.getName().startsWith(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX)) {
        filterParameter.setName(mapperSolrToDisplayName.get(filterParameter.getName()));
      }
    }

    return filter;
  }

  private String getTableIdFromFilter(Filter filter) {
    for (FilterParameter filterParameter : filter.getParameters()) {
      if (filterParameter.getName().equals(ViewerConstants.SOLR_ROWS_TABLE_ID)
          && filterParameter instanceof SimpleFilterParameter) {
        return ((SimpleFilterParameter) filterParameter).getValue();
      }
    }
    return null;
  }

  private Map<String, String> getDisplayNameColumn(ViewerMetadata metadata, Filter filter) {
    Map<String, String> solrNameToDisplayName = new HashMap<>();

    String tableId = getTableIdFromFilter(filter);

    for (FilterParameter filterParameter : filter.getParameters()) {
      if (filterParameter.getName().startsWith(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX)) {
        final List<ViewerColumn> columns = metadata.getTableById(tableId).getColumns();

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
