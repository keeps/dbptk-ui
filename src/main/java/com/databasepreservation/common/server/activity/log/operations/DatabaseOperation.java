package com.databasepreservation.common.server.activity.log.operations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DatabaseOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    try {
      ActivityLogEntry log = wrapper.getActivityLogEntry();
      final ViewerDatabase viewerDatabase = getViewerDatabase(log.getParameters());
      if (viewerDatabase != null) {
        wrapper.setDatabase(viewerDatabase);
        wrapper.setDatabasePresence(PresenceState.YES);
      } else {
        wrapper.setDatabasePresence(PresenceState.NO);
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.debug("Error executing the retrieve database information", e);
    }

    return wrapper;
  }

  private ViewerDatabase getViewerDatabase(Map<String, String> parameters)
    throws GenericException, RequestNotValidException {

    final String databaseUuid = parameters.get(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM);

    if (databaseUuid == null)
      return null;

    List<String> fieldsToReturn = Arrays.asList(ViewerConstants.INDEX_ID, ViewerConstants.SOLR_DATABASES_METADATA,
      ViewerConstants.SOLR_DATABASES_STATUS);
    Filter filterParam = new Filter(new SimpleFilterParameter(ViewerConstants.INDEX_ID, databaseUuid));
    final IndexResult<ViewerDatabase> viewerDatabase = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
      filterParam, Sorter.NONE, new Sublist(), Facets.NONE, fieldsToReturn);

    if (viewerDatabase.getTotalCount() == 0)
      return null;

    return viewerDatabase.getResults().get(0);
  }
}
