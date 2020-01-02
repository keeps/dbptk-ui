package com.databasepreservation.common.server.activity.log.strategies;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NoLogStrategy extends ActivityLogStrategy {

  public NoLogStrategy(){
    super();
  }

  @Override
  public ActivityLogWrapper apply(ActivityLogWrapper wrapper) {
    wrapper.setParameters(false);
    return wrapper;
  }
}
