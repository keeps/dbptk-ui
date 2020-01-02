package com.databasepreservation.common.server.activity.log.strategies;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IdentityLogStrategy extends ActivityLogStrategy {

  public IdentityLogStrategy() {
    super();
  }

  @Override
  public ActivityLogWrapper apply(ActivityLogWrapper wrapper) {
    return wrapper;
  }
}
