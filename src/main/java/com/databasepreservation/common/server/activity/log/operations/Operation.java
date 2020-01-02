package com.databasepreservation.common.server.activity.log.operations;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface Operation {

  ActivityLogWrapper execute(ActivityLogWrapper wrapper);
}
