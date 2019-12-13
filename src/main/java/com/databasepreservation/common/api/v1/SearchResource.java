package com.databasepreservation.common.api.v1;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.common.utils.BrowserServiceUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.user.User;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.services.SearchService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.UserUtility;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
@Path(ViewerConstants.ENDPOINT_SEARCH)
public class SearchResource implements SearchService {
  @Context
  private HttpServletRequest request;

  @Override
  public String saveSearch(String databaseUUID, String tableUUID, String tableName, String name, String description, SearchInfo searchInfo) throws RESTException {
    try {
      UserUtility.Authorization.checkDatabaseAccessPermission(request, databaseUUID);
    } catch (AuthorizationDeniedException | GenericException | NotFoundException e) {
      throw new RESTException(e.getMessage());
    }

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
    } catch (NotFoundException | GenericException e) {
      throw new RESTException(e.getMessage());
    }

    return savedSearch.getUuid();
  }

  @Override
  public IndexResult<SavedSearch> findSavedSearches(String databaseUUID, FindRequest findRequest, String localeString) throws RESTException {
    try {
      UserUtility.Authorization.checkFilteringPermission(request, databaseUUID, findRequest.filter,
        SavedSearch.class);
    } catch (AuthorizationDeniedException | NotFoundException | GenericException e) {
      throw new RESTException(e.getMessage());
    }

    try {
      return ViewerFactory.getSolrManager().find(SavedSearch.class, findRequest.filter, findRequest.sorter,
        findRequest.sublist, findRequest.facets);
    } catch (GenericException | RequestNotValidException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public SavedSearch retrieveSavedSearch(String databaseUUID, String savedSearchUUID) throws RESTException {
    try {
      SavedSearch result = ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
      UserUtility.Authorization.checkRetrievalPermission(request, databaseUUID, SavedSearch.class, result);
      return result;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    }

  }

  @Override
  public void editSearch(String databaseUUID, String savedSearchUUID, String name, String description)
    throws RESTException {
    // get the saved search
    SavedSearch savedSearch = null;
    try {
      savedSearch = ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
      // authorise viewing the saved search
      UserUtility.Authorization.checkSavedSearchPermission(request, databaseUUID, savedSearch);
      // authorise editing the saved search
      UserUtility.Authorization.allowIfAdminOrManager(request);

      ViewerFactory.getSolrManager().editSavedSearch(savedSearchUUID, name, description);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public void deleteSearch(String databaseUUID, String savedSearchUUID) throws RESTException {
    // get the saved search
    try {
      SavedSearch savedSearch = ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
      // authorise viewing the saved search
      UserUtility.Authorization.checkSavedSearchPermission(request, databaseUUID, savedSearch);
      // authorise editing the saved search
      UserUtility.Authorization.allowIfAdminOrManager(request);

      ViewerFactory.getSolrManager().deleteSavedSearch(savedSearchUUID);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public List<SearchField> getSearchFields(ViewerTable viewerTable) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);

    LogEntryState state = LogEntryState.SUCCESS;

    // register action
    controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, viewerTable.getName());
    return BrowserServiceUtils.getSearchFieldsFromTable(viewerTable);
  }
}
