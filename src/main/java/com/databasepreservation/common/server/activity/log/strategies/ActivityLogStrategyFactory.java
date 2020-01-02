package com.databasepreservation.common.server.activity.log.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.server.activity.log.operations.ColumnOperation;
import com.databasepreservation.common.server.activity.log.operations.DatabaseOperation;
import com.databasepreservation.common.server.activity.log.operations.ExportRequestOperation;
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
        case "findRows":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new FacetsOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "findDatabases":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new FacetsOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "retrieveRow":
        case "retrieve":
        case "deleteSolrData":
        case "deleteDatabase":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_EXPORT_RESOURCE.equals(actionComponent)) {
      if ("getCSVResultsPost".equals(actionMethod)) {
        composeLogStrategy.clearOperationList();
        composeLogStrategy.getOperationList().add(new DatabaseOperation());
        composeLogStrategy.getOperationList().add(new TableOperation());
        composeLogStrategy.getOperationList().add(new FilterOperation());
        composeLogStrategy.getOperationList().add(new SublistOperation());
        composeLogStrategy.getOperationList().add(new ExportRequestOperation());
        return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_LOB_RESOURCE.equals(actionComponent)) {
      if ("getLOB".equals(actionMethod)) {
        composeLogStrategy.clearOperationList();
        composeLogStrategy.getOperationList().add(new DatabaseOperation());
        composeLogStrategy.getOperationList().add(new TableOperation());
        composeLogStrategy.getOperationList().add(new ColumnOperation());
        composeLogStrategy.getOperationList().add(new RowOperation());
        return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_FILE_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "createSIARDFile":
          return noLogStrategy;
        case "getSIARDFile":
        case "getValidationReportFile":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_SEARCH_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "delete":
        case "edit":
        case "retrieve":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new SearchOperation());
          return composeLogStrategy;
        case "find":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new FilterOperation());
          composeLogStrategy.getOperationList().add(new SublistOperation());
          return composeLogStrategy;
        case "getSearchFields":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          return composeLogStrategy;
        case "save":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          composeLogStrategy.getOperationList().add(new TableOperation());
          composeLogStrategy.getOperationList().add(new SearchOperation());
          return composeLogStrategy;
      }
    } else if (ViewerConstants.CONTROLLER_SIARD_RESOURCE.equals(actionComponent)) {
      switch (actionMethod) {
        case "deleteSIARDFile":
        case "deleteSIARDValidatorReportFile":
        case "updateMetadataInformation":
        case "validateSIARD":
        case "updateStatusValidate":
          composeLogStrategy.clearOperationList();
          composeLogStrategy.getOperationList().add(new DatabaseOperation());
          return composeLogStrategy;
        case "uploadMetadataSIARDServer":
        case "uploadSIARD":
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
    }

    return identityLogStrategy;
  }
}
