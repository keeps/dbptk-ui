package com.databasepreservation.common.client.common.utils;

import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_ACTIVITY_LOG_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_DATABASE_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_FILE_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_SEARCH_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_SIARD_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_USER_LOGIN_CONTROLLER;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogUtils {

  public static void getParametersHTML(ActivityLogEntry entry) {
    switch (entry.getActionComponent()) {
      case CONTROLLER_ACTIVITY_LOG_RESOURCE:
        getLogParameters(entry);
        break;
      case CONTROLLER_DATABASE_RESOURCE:
        getDatabaseParameters(entry);
        break;
      case CONTROLLER_FILE_RESOURCE:
        getFileParameters(entry);
        break;
      case CONTROLLER_SIARD_RESOURCE:
        getSIARDParameters(entry);
        break;
      case CONTROLLER_SEARCH_RESOURCE:
        getSearchParameters(entry);
        break;
      case CONTROLLER_USER_LOGIN_CONTROLLER:
        getLoginParameters(entry);
        break;
      default:
        break;
    }
  }

  private static void getLogParameters(ActivityLogEntry entry) {

  }

  private static void getDatabaseParameters(ActivityLogEntry entry) {
  }

  private static void getFileParameters(ActivityLogEntry entry) {
  }

  private static void getSIARDParameters(ActivityLogEntry entry) {
  }

  private static void getSearchParameters(ActivityLogEntry entry) {
  }

  private static void getLoginParameters(ActivityLogEntry entry) {
  }
}
