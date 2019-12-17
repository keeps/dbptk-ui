package com.databasepreservation.common.api.v1;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.utils.AdvancedSearchUtils;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.exceptions.SavedSearchException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.SearchService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
@Path(ViewerConstants.ENDPOINT_SEARCH)
public class SearchResource implements SearchService {
  @Context
  private HttpServletRequest request;

  @Override
  public String save(String databaseUUID, String tableUUID, String tableName, String name, String description,
                     SearchInfo searchInfo) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    String searchInfoJson = JsonUtils.getJsonFromObject(searchInfo);

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setUuid(SolrUtils.randomUUID());
    savedSearch.setName(name);
    savedSearch.setDescription(description);
    savedSearch.setDatabaseUUID(databaseUUID);
    savedSearch.setTableUUID(tableUUID);
    savedSearch.setTableName(tableName);
    savedSearch.setSearchInfoJson(searchInfoJson);

    try {
      ViewerFactory.getSolrManager().addSavedSearch(savedSearch);
      return savedSearch.getUuid();
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_TABLE_ID_PARAM, tableUUID, ViewerConstants.CONTROLLER_SAVED_SEARCH_NAME_PARAM, name,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_DESCRIPTION_PARAM, description,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_PARAM, savedSearch.getSearchInfoJson());
    }
  }

  @Override
  public IndexResult<SavedSearch> find(String databaseUUID, FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().find(SavedSearch.class, findRequest.filter, findRequest.sorter,
        findRequest.sublist, findRequest.facets);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_FILTER_PARAM, JsonUtils.getJsonFromObject(findRequest));
    }
  }

  @Override
  public SavedSearch retrieve(String databaseUUID, String savedSearchUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      return ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
    } catch (NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID);
    }
  }

  @Override
  public void edit(String databaseUUID, String savedSearchUUID, String name, String description) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      ViewerFactory.getSolrManager().editSavedSearch(savedSearchUUID, name, description);
    } catch (SavedSearchException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_NAME_PARAM, name,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_DESCRIPTION_PARAM, description);
    }
  }

  @Override
  public void delete(String databaseUUID, String savedSearchUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      ViewerFactory.getSolrManager().deleteSavedSearch(savedSearchUUID);
    } catch (SavedSearchException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID,
        ViewerConstants.CONTROLLER_SAVED_SEARCH_UUID_PARAM, savedSearchUUID);
    }
  }

  @Override
  public List<SearchField> getSearchFields(ViewerTable viewerTable) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);
    try {
      return AdvancedSearchUtils.getSearchFieldsFromTable(viewerTable);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        viewerTable.getName());
    }
  }
}
