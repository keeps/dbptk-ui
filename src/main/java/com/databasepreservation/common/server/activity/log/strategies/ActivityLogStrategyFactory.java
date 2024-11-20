/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.activity.log.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.server.activity.log.operations.ColumnOperation;
import com.databasepreservation.common.server.activity.log.operations.DatabaseOperation;
import com.databasepreservation.common.server.activity.log.operations.FacetsOperation;
import com.databasepreservation.common.server.activity.log.operations.FilterOperation;
import com.databasepreservation.common.server.activity.log.operations.RowOperation;
import com.databasepreservation.common.server.activity.log.operations.SearchOperation;
import com.databasepreservation.common.server.activity.log.operations.SublistOperation;
import com.databasepreservation.common.server.activity.log.operations.TableOperation;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogStrategyFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogStrategyFactory.class);

  private final ActivityLogStrategy noLogStrategy;
  private final ActivityLogStrategy identityLogStrategy;
  private final ActivityLogStrategy composeLogStrategy;

  public ActivityLogStrategyFactory() {
    noLogStrategy = new NoLogStrategy();
    identityLogStrategy = new IdentityLogStrategy();
    composeLogStrategy = new ComposeLogStrategy();
  }

  public ActivityLogStrategy getStrategy(String actionComponent, String actionMethod) {
    if (ViewerConstants.CONTROLLER_DATABASE_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "createDatabase":
          return identityLogStrategy;
        case "retrieve":
        case "deleteDatabase":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          return composeLogStrategy;
        case "findDatabases":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new FacetsOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "findAll":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new FilterOperation());
          return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_COLLECTION_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "createCollection":
        case "getProgressData":
        case "getReport":
        case "deleteCollection":
        case "getCollectionConfiguration":
        case "updateCollectionConfiguration":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          return composeLogStrategy;
        case "getDenormalizeConfigurationFile":
        case "createDenormalizeConfigurationFile":
        case "deleteDenormalizeConfigurationFile":
        case "run":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          return composeLogStrategy;
        case "findRows":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new FacetsOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "retrieveRow":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new RowOperation());
          return composeLogStrategy;
        case "exportLOB":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          composeLogStrategy.getOperationList().add(new ColumnOperation());
          composeLogStrategy.getOperationList().add(new RowOperation());
          return composeLogStrategy;
        case "exportFindToCSV":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "exportSingleRowToCSV":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          composeLogStrategy.getOperationList().add(new RowOperation());
          return composeLogStrategy;
        case "deleteSavedSearch":
        case "updateSavedSearch":
        case "retrieveSavedSearch":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new SearchOperation());
          return composeLogStrategy;
        case "findSavedSearches":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "saveSavedSearch":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          composeLogStrategy.getOperationList().add(new SearchOperation());
          return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_FILE_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "list":
          return noLogStrategy;
        case "getSIARDFile":
        case "deleteSiardFile":
        case "createSIARDFile":
        default:
          return identityLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_USER_LOGIN_CONTROLLER.equals(actionComponent)) {
      switch (actionMethod) {
        case "casLogin":
        case "logout":
          return identityLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_ACTIVITY_LOG_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "find":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new FacetsOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "retrieve":
          return identityLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_SIARD_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "getValidationProgressData":
        case "deleteSIARDFile":
        case "deleteValidationReport":
        case "updateMetadataInformation":
        case "validateSiard":
        case "getValidationReportFile":
        case "getMetadataInformation":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_JOB_RESOURCE.equals(actionComponent)){
      composeLogStrategy.clearOperationList();
      composeLogStrategy.getOperationList().add(new FilterOperation());
      return composeLogStrategy;
    }

    return identityLogStrategy;
  }
}
