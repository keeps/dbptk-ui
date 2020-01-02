package com.databasepreservation.common.server.activity.log.strategies;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.server.activity.log.operations.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class ActivityLogStrategy {
  protected List<Operation> operationList;

  public ActivityLogStrategy() {
    operationList = new ArrayList<>();
  }

  public ActivityLogWrapper apply(ActivityLogWrapper wrapper) {
    for (Operation operation : operationList) {
      operation.execute(wrapper);
    }
    return wrapper;
  }

  protected List<Operation> getOperationList() {
    return operationList;
  }

  protected void clearOperationList() {
    operationList.clear();
    operationList = new ArrayList<>();
  }
}
