package com.databasepreservation.common.server.activity.log.operations;

import java.util.Arrays;
import java.util.List;

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
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RowOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(RowOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    final String rowUUID = wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_ROW_ID_PARAM);

    if (wrapper.getDatabase() != null) {
      try {
        List<String> fieldsToReturn = Arrays.asList(ViewerConstants.INDEX_ID, ViewerConstants.SOLR_ROWS_TABLE_ID);
        Filter filterParam = new Filter(new SimpleFilterParameter(ViewerConstants.INDEX_ID, rowUUID));
        final IndexResult<ViewerRow> viewerRow = ViewerFactory.getSolrManager().findRows(wrapper.getDatabase().getUuid(),
            filterParam, Sorter.NONE, new Sublist(), Facets.NONE, fieldsToReturn);

        if (viewerRow.getTotalCount() == 0) {
          wrapper.setRowPresence(PresenceState.NO);
        } else {
          wrapper.setRow(viewerRow.getResults().get(0));
          wrapper.setRowPresence(PresenceState.YES);
        }

      } catch (GenericException | RequestNotValidException e) {
        LOGGER.debug("Error executing the retrieve database information", e);
      }
    }

    return wrapper;
  }
}
