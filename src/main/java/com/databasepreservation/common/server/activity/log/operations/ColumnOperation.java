/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.activity.log.operations;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.client.models.structure.ViewerColumn;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnOperation implements Operation {
  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    int index = Integer.parseInt(wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_COLUMN_ID_PARAM));

    if (wrapper.getTable() == null) {
      wrapper.setTablePresence(PresenceState.NO);
      return wrapper;
    }

    final ViewerColumn column = wrapper.getTable().getColumnByIndexInEnclosingTable(index);

    if (column == null) {
      wrapper.setColumnPresence(PresenceState.NO);
    } else {
      wrapper.setColumnPresence(PresenceState.YES);
      wrapper.setColumnName(column.getDisplayName());
    }

    return wrapper;
  }
}
