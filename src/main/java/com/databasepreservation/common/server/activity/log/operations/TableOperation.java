package com.databasepreservation.common.server.activity.log.operations;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableOperation implements Operation {
  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    final String tableUUID = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_TABLE_ID_PARAM);

    if (tableUUID != null) {
      if (wrapper.getDatabase() != null) {
        ViewerTable table = wrapper.getDatabase().getMetadata().getTableById(tableUUID);
        wrapper.setTable(table);
        wrapper.setTablePresence(PresenceState.YES);
      } else {
        wrapper.setTablePresence(PresenceState.NO);
      }
    }

    return wrapper;
  }
}
