/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.FacetFieldResult;
import com.databasepreservation.common.client.index.facets.FacetParameter;
import com.databasepreservation.common.client.index.facets.FacetValue;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.facets.SimpleFacetParameter;
import com.databasepreservation.common.client.index.filter.AndFiltersParameters;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.OrFiltersParameters;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_DATABASE)
public class DatabaseResource implements DatabaseService {
  @Autowired
  private HttpServletRequest request;

  @Override
  public IndexResult<ViewerDatabase> find(FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    if (ViewerConfiguration.getInstance().getApplicationEnvironment().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      if (user.isAdmin() || user.isWhiteList()) {
        return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
      } else {
        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_PERMISSIONS);

        FindRequest userFindRequest = new FindRequest(findRequest.classToReturn,
          getDatabaseFilterForUser(user, findRequest.filter, true), findRequest.sorter, findRequest.sublist,
          findRequest.facets, findRequest.exportFacets, fieldsToReturn);
        return getViewerDatabaseIndexResult(userFindRequest, fieldsToReturn, controllerAssistant, user, state);
      }
    } else {
      return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
    }
  }

  @Override
  public IndexResult<ViewerDatabase> findAll(FindRequest findRequest, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);
    if (ViewerConfiguration.getInstance().getApplicationEnvironment().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      if (user.isAdmin() || user.isWhiteList()) {
        return getCrossViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
      } else {
        return getCrossViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state,
          getDatabaseFilterForUser(user, findRequest.filter, false));
      }
    } else {
      return getCrossViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
    }
  }

  @Override
  public StringResponse create(String path, String siardVersion) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      return new StringResponse(SIARDController.loadMetadataFromLocal(path, siardVersion));
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILENAME_PARAM, path);
    }
  }

  private IndexResult<ViewerDatabase> getViewerDatabaseIndexResult(FindRequest findRequest,
    ControllerAssistant controllerAssistant, User user, LogEntryState state) {
    long count = 0;
    try {
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, findRequest.fieldsToReturn);
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
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, fieldsToReturn);
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

  private IndexResult<ViewerDatabase> getCrossViewerDatabaseIndexResult(FindRequest findRequest,
    ControllerAssistant controllerAssistant, User user, LogEntryState state) {
    return getCrossViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state, null);
  }

  private IndexResult<ViewerDatabase> getCrossViewerDatabaseIndexResult(FindRequest findRequest,
    ControllerAssistant controllerAssistant, User user, LogEntryState state, Filter userFilter) {
    long count = 0;
    try {
      IterableDatabaseResult<ViewerDatabase> databases;
      if (userFilter != null) {
        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_STATUS);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_PERMISSIONS);
        databases = ViewerFactory.getSolrManager().findAll(ViewerDatabase.class, userFilter, Sorter.NONE,
          fieldsToReturn);
      } else {
        databases = ViewerFactory.getSolrManager().findAll(ViewerDatabase.class, new Filter(), Sorter.NONE,
          findRequest.fieldsToReturn);
      }

      if (databases.getTotalCount() == 0) {
        return new IndexResult<>();
      }

      // Search on all collections
      List<String> collections = new ArrayList<>();
      Map<String, ViewerDatabase> databaseMap = new HashMap<>();
      for (ViewerDatabase database : databases) {
        databaseMap.put(database.getUuid(), database);
        // only add the available collections
        if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
          String collectionName = ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + database.getUuid();
          collections.add(collectionName);
        }
      }

      String collectionAlias = SolrUtils.crateSearchAllAlias(ViewerFactory.getSolrClient(), "alias-" + user.getUUID(),
        collections);

      SimpleFacetParameter simpleFacetParameter = new SimpleFacetParameter(ViewerConstants.SOLR_ROWS_DATABASE_UUID,
        FacetParameter.SORT.COUNT);
      simpleFacetParameter.setMinCount(1);
      simpleFacetParameter.setLimit(findRequest.sublist.getMaximumElementCount());
      simpleFacetParameter.setOffset(findRequest.sublist.getFirstElementIndex());

      final IndexResult<ViewerDatabase> facetsSearch = ViewerFactory.getSolrManager().findHits(ViewerDatabase.class,
        collectionAlias, findRequest.filter, findRequest.sorter, findRequest.sublist, new Facets(simpleFacetParameter));
      count = facetsSearch.getTotalCount();
      FacetFieldResult facetResults = facetsSearch.getFacetResults().get(0);

      IndexResult<ViewerDatabase> searchHitsResult = new IndexResult<>();
      if (facetResults.getValues().size() < findRequest.sublist.getMaximumElementCount()
        && findRequest.sublist.getFirstElementIndex() == 0) {
        searchHitsResult.setTotalCount(facetResults.getValues().size());
      } else {
        searchHitsResult.setTotalCount(-1);
      }
      searchHitsResult.setLimit(findRequest.sublist.getMaximumElementCount());
      searchHitsResult.setOffset(findRequest.sublist.getFirstElementIndex());
      List<ViewerDatabase> resultsFromFacet = new ArrayList<>();

      // Retrieve the databases HITs
      for (FacetValue value : facetResults.getValues()) {
        String databaseUUID = value.getValue();
        long searchHits = value.getCount();

        ViewerDatabase vd = databaseMap.get(databaseUUID);
        vd.setSearchHits(searchHits);
        resultsFromFacet.add(vd);
      }

      searchHitsResult.setResults(resultsFromFacet);
      return searchHitsResult;

    } catch (GenericException | RequestNotValidException | SolrServerException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  private Filter getDatabaseFilterForUser(User user, Filter searchFilter, boolean addSearchFilter) {
    ArrayList<FilterParameter> permissionFilterParameters = new ArrayList<>();
    // Only retrieve databases with AVAILABLE status
    SimpleFilterParameter statusFilter = new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS,
      ViewerDatabaseStatus.AVAILABLE.name());
    permissionFilterParameters.add(statusFilter);

    if (!addSearchFilter) {
      permissionFilterParameters.add(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL, "true"));
    }

    // Add user permissions on filter
    ArrayList<FilterParameter> permissionsOrFilterParameters = new ArrayList<>();
    for (String role : user.getAllRoles()) {
      permissionsOrFilterParameters.add(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_PERMISSIONS, role));
    }

    permissionFilterParameters.add(new OrFiltersParameters(permissionsOrFilterParameters));

    if (!searchFilter.getParameters().isEmpty() && addSearchFilter) {
      BasicSearchFilterParameter searchFilterParameter = (BasicSearchFilterParameter) searchFilter.getParameters()
        .get(0);
      String searchValue = searchFilterParameter.getValue();
      permissionFilterParameters.add(new SimpleFilterParameter(ViewerConstants.INDEX_SEARCH, searchValue));
    }

    return new Filter(new AndFiltersParameters(permissionFilterParameters));
  }

  @Override
  public ViewerDatabase retrieve(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    UserUtility.checkDatabasePermission(user, databaseUUID);

    try {
      return ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
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
  public Boolean delete(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

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
  public Set<String> getDatabasePermissions(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      DatabaseStatus databaseStatus = ViewerFactory.getConfigurationManager().getDatabaseStatus(databaseUUID);
      return databaseStatus.getPermissions();
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }

  }

  @Override
  public Set<String> updateDatabasePermissions(String databaseUUID, Set<String> permissions) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      return SIARDController.updateDatabasePermissions(databaseUUID, permissions);
    } catch (GenericException | ViewerException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public boolean updateDatabaseSearchAllAvailability(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      return SIARDController.updateDatabaseSearchAllAvailability(databaseUUID);
    } catch (GenericException | ViewerException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }

  }

}
