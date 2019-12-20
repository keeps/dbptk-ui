package com.databasepreservation.common.client.models.activity.logs;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogWrapper implements Serializable {

  private ActivityLogEntry activityLogEntry;

  public ActivityLogWrapper() {
  }

  public ActivityLogWrapper(ActivityLogEntry entry) {
    this.activityLogEntry = entry;
  }

  public ActivityLogEntry getActivityLogEntry() {
    return activityLogEntry;
  }

  public void setActivityLogEntry(ActivityLogEntry activityLogEntry) {
    this.activityLogEntry = activityLogEntry;
  }

}
