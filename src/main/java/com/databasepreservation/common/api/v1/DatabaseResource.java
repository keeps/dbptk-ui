package com.databasepreservation.common.api.v1;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
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
import com.databasepreservation.common.server.ViewerConfiguration;
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
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return SIARDController.getDatabaseMetadata(connectionParameters);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  @Override
  public List<List<String>> validateCustomViewQuery(ConnectionParameters parameters, String query) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    final User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return SIARDController.validateCustomViewQuery(parameters, query);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
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

    controllerAssistant.checkRoles(user);

    if (ViewerConfiguration.getInstance().getApplicationEnvironment().equals(ViewerConstants.SERVER)) {
      if (UserUtility.userIsAdmin(request)) {
        return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
      } else {
        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
        FilterParameter parameter = new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS,
          ViewerDatabaseStatus.AVAILABLE.name());
        Filter databasesReadyFilter = new Filter(parameter);
        FindRequest request = new FindRequest(findRequest.classToReturn, databasesReadyFilter, findRequest.sorter,
          findRequest.sublist, findRequest.facets, findRequest.exportFacets, fieldsToReturn);
        return getViewerDatabaseIndexResult(request, fieldsToReturn, controllerAssistant, user, state);
      }
    } else {
      return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
    }
  }

  private IndexResult<ViewerDatabase> getViewerDatabaseIndexResult(FindRequest findRequest,
    ControllerAssistant controllerAssistant, User user, LogEntryState state) {
    long count = 0;
    try {
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
      count = result.getTotalCount();
      return result;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  private IndexResult<ViewerDatabase> getViewerDatabaseIndexResult(FindRequest findRequest, List<String> fieldsToReturn,
    ControllerAssistant controllerAssistant, User user, LogEntryState state) {
    long count = 0;
    try {
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class, findRequest.filter, findRequest.sorter,
          findRequest.sublist, findRequest.facets, fieldsToReturn);
      count = result.getTotalCount();
      return result;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  @Override
  public ViewerDatabase retrieve(String databaseUUID, String id) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, id);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public Boolean deleteDatabase(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;
    controllerAssistant.checkRoles(user);

    try {
      return SIARDController.deleteAll(databaseUUID);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public Boolean deleteSolrData(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
          savedSearchFilter);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        return true;
      }
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
    return false;
  }

  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, FindRequest findRequest, String localeString)
    throws RESTException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    long count = 0;

    try {
      final IndexResult<ViewerRow> viewerRowIndexResult = ViewerFactory.getSolrManager().findRows(databaseUUID,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
      count = viewerRowIndexResult.getTotalCount();
      return viewerRowIndexResult;
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_FILTER_PARAM, JsonUtils.getJsonFromObject(findRequest.filter),
        ViewerConstants.CONTROLLER_SUBLIST_PARAM, JsonUtils.getJsonFromObject(findRequest.sublist),
        ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  @Override
  public ViewerRow retrieveRow(String databaseUUID, String rowUUID) throws RESTException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowUUID);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID, ViewerConstants.CONTROLLER_ROW_ID_PARAM, rowUUID);
    }
  }
}
