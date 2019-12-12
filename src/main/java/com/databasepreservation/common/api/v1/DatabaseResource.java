package com.databasepreservation.common.api.v1;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.ProgressData;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_DATABASE)
public class DatabaseResource implements DatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResource.class);
  @Context
  private HttpServletRequest request;

  @Override
  public String generateUUID() {
    return SolrUtils.randomUUID();
  }

  @Override
  public ViewerMetadata getSchemaInformation(ConnectionParameters connectionParameters) {
    try {
      return SIARDController.getDatabaseMetadata(connectionParameters);
    } catch (GenericException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public List<List<String>> validateCustomViewQuery(ConnectionParameters parameters, String query) {
    try {
      return SIARDController.validateCustomViewQuery(parameters, query);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public ProgressData getProgressData(String databaseuuid) {
    return ProgressData.getInstance(databaseuuid);
  }

  @Override
  public IndexResult<ViewerDatabase> findDatabases(FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    if (UserUtility.userIsAdmin(request)) {
      try {
        return ViewerFactory.getSolrManager().find(ViewerDatabase.class, findRequest.filter, findRequest.sorter,
          findRequest.sublist, findRequest.facets);
      } catch (GenericException | RequestNotValidException e) {
        throw new RESTException(e.getMessage());
      } finally {
        // register action
        controllerAssistant.registerAction(user, state);
      }
    } else {
      List<String> fieldsToReturn = new ArrayList<>();
      fieldsToReturn.add(ViewerConstants.INDEX_ID);
      fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
      try {
        return ViewerFactory.getSolrManager().find(ViewerDatabase.class, findRequest.filter, findRequest.sorter,
          findRequest.sublist, findRequest.facets, fieldsToReturn);
      } catch (GenericException | RequestNotValidException e) {
        throw new RESTException(e.getMessage());
      } finally {
        // register action
        controllerAssistant.registerAction(user, state);
      }
    }
  }

  @Override
  public ViewerDatabase retrieve(String databaseUUID, String id) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, id);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean deleteDatabase(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      controllerAssistant.checkRoles(user);
      return SIARDController.deleteAll(databaseUUID);
    } catch (AuthorizationDeniedException e) {
      // TODO change to other exception and add exception mapper to return a different
      // response error code
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
  }

  @Override
  public Boolean deleteSolrData(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(user);
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
            savedSearchFilter);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        return true;
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error trying to remove the collection from Solr", e);
      return false;
    } catch (AuthorizationDeniedException e) {
      e.printStackTrace();
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }
    return false;
  }

  //
  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return ViewerFactory.getSolrManager().findRows(databaseUUID, findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e.getMessage());
    } catch (RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException("Invalid database UUID: " + databaseUUID);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_FILTER_PARAM, findRequest.filter.toString());
    }
  }

  @Override
  public ViewerRow retrieveRows(String databaseUUID, String rowUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowUUID);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e.getMessage());
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
          ViewerConstants.CONTROLLER_ROW_ID_PARAM, rowUUID);
    }
  }
}
