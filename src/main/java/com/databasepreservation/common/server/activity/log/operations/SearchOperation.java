package com.databasepreservation.common.server.activity.log.operations;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SearchOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    final String searchUUID = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM);

    if (searchUUID == null) {
      final String savedSearchJson = wrapper.getActivityLogEntry().getParameters()
        .get(ViewerConstants.CONTROLLER_SAVED_SEARCH_PARAM);
      try {
        final SavedSearch savedSearch = JsonUtils.getObjectFromJson(savedSearchJson, SavedSearch.class);
        if (savedSearch != null) {
          wrapper.setSavedSearch(savedSearch);
          wrapper.setSavedSearchPresence(PresenceState.YES);
        } else {
          wrapper.setSavedSearchPresence(PresenceState.NO);
        }
      } catch (GenericException e) {
        LOGGER.debug("Error executing the retrieve saved search information", e);
      }
    } else {
      if (wrapper.getDatabase() != null) {
        try {
          List<String> fieldsToReturn = Arrays.asList(ViewerConstants.INDEX_ID,
            ViewerConstants.SOLR_SEARCHES_DATABASE_UUID, ViewerConstants.SOLR_SEARCHES_NAME);
          Filter filterParam = new Filter(new SimpleFilterParameter(ViewerConstants.INDEX_ID, searchUUID));
          final IndexResult<SavedSearch> result = ViewerFactory.getSolrManager().find(SavedSearch.class, filterParam,
            Sorter.NONE, new Sublist(), Facets.NONE, fieldsToReturn);

          if (result.getTotalCount() == 0) {
            wrapper.setSavedSearchPresence(PresenceState.NO);
          } else {
            wrapper.setSavedSearch(result.getResults().get(0));
            wrapper.setSavedSearchPresence(PresenceState.YES);
          }

        } catch (GenericException | RequestNotValidException e) {
          LOGGER.debug("Error executing the retrieve database information", e);
        }
      }
    }

    return wrapper;
  }
}
