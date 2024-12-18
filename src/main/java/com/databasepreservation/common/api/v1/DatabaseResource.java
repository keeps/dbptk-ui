/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.FacetFieldResult;
import com.databasepreservation.common.client.index.facets.FacetParameter;
import com.databasepreservation.common.client.index.facets.FacetValue;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.facets.SimpleFacetParameter;
import com.databasepreservation.common.client.index.filter.BlockJoinAnyParentExpiryFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.exceptions.AuthorizationException;
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
    User user;

    try {
      user = controllerAssistant.checkRoles(request);
    } catch (AuthorizationException e) {
      throw new RESTException(e);
    }

    if (ViewerConfiguration.getInstance().getApplicationEnvironment().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      if (user.isAdmin() || user.isWhiteList()) {
        return getViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
      } else {
        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_PERMISSIONS);

        FindRequest userFindRequest = new FindRequest(findRequest.classToReturn, findRequest.filter, findRequest.sorter,
          findRequest.sublist, findRequest.facets, findRequest.exportFacets, fieldsToReturn);
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
    User user;

    try {
      user = controllerAssistant.checkRoles(request);
    } catch (AuthorizationException e) {
      throw new RESTException(e);
    }

    return getCrossViewerDatabaseIndexResult(findRequest, controllerAssistant, user, state);
  }

  @Override
  public StringResponse create(String path, ViewerConstants.SiardVersion siardVersion) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      return new StringResponse(SIARDController.loadMetadataFromLocal(path, siardVersion));
    } catch (GenericException | AuthorizationException e) {
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
      ArrayList<Filter> filterQueries = new ArrayList<>();
      filterQueries.addAll(getDatabaseFindContentTypeFilterQueries());
      if (!user.isAdmin() && !user.isWhiteList()) {
        filterQueries.addAll(getDatabaseFindStatusFilterQueries(findRequest.filter));
        filterQueries.addAll(getDatabaseFindUserPermissionsFilterQueries(user));
      }
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, findRequest.fieldsToReturn,
        filterQueries);
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
      ArrayList<Filter> filterQueries = new ArrayList<>();
      filterQueries.addAll(getDatabaseFindContentTypeFilterQueries());
      if (!user.isAdmin() && !user.isWhiteList()) {
        filterQueries.addAll(getDatabaseFindStatusFilterQueries(findRequest.filter));
        filterQueries.addAll(getDatabaseFindUserPermissionsFilterQueries(user));
      }
      final IndexResult<ViewerDatabase> result = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, fieldsToReturn, filterQueries);
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
    long count = 0;
    try {
      IterableDatabaseResult<ViewerDatabase> databases;
      ArrayList<Filter> filterQueries = new ArrayList<>();
      filterQueries.addAll(getDatabaseFindContentTypeFilterQueries());
      filterQueries.addAll(getDatabaseFindAllFilterQueries());
      if (!user.isAdmin() && !user.isWhiteList()) {
        filterQueries.addAll(getDatabaseFindStatusFilterQueries(findRequest.filter));
        filterQueries.addAll(getDatabaseFindUserPermissionsFilterQueries(user));
      }
      if (findRequest.filter != null) {
        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_STATUS);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_PERMISSIONS);
        databases = ViewerFactory.getSolrManager().findAll(ViewerDatabase.class, new Filter(), Sorter.NONE,
          fieldsToReturn, filterQueries);
      } else {
        databases = ViewerFactory.getSolrManager().findAll(ViewerDatabase.class, new Filter(), Sorter.NONE,
          findRequest.fieldsToReturn, filterQueries);
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

      if (collections.isEmpty()) {
        return new IndexResult<>();
      }

      String collectionAlias = SolrUtils.createSearchAllAlias(ViewerFactory.getSolrClient(),
        ViewerConstants.ALIAS_PREFIX + UUID.randomUUID(), collections);

      SimpleFacetParameter simpleFacetParameter = new SimpleFacetParameter(ViewerConstants.SOLR_ROWS_DATABASE_UUID,
        FacetParameter.SORT.COUNT);
      simpleFacetParameter.setMinCount(1);
      simpleFacetParameter.setLimit(findRequest.sublist.getMaximumElementCount());
      simpleFacetParameter.setOffset(findRequest.sublist.getFirstElementIndex());

      final IndexResult<ViewerDatabase> facetsSearch = ViewerFactory.getSolrManager().findHits(ViewerDatabase.class,
        collectionAlias, findRequest.filter, findRequest.sorter, findRequest.sublist, new Facets(simpleFacetParameter));

      SolrUtils.deleteSearchAllAlias(ViewerFactory.getSolrClient(), collectionAlias);

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

      databases.close();

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

  private List<Filter> getDatabaseFindStatusFilterQueries(Filter searchFilter) {
    // Only retrieve databases with AVAILABLE status, unless the searchFilter
    // already has this filter
    Filter statusFilter = new Filter();
    SimpleFilterParameter statusFilterParameter;
    statusFilterParameter = new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS,
      ViewerDatabaseStatus.AVAILABLE.name());
    if (searchFilter != null) {
      for (FilterParameter filterParameter : searchFilter.getParameters()) {
        if (filterParameter instanceof SimpleFilterParameter simpleFilterParameter
          && simpleFilterParameter.getName().equals(ViewerConstants.SOLR_DATABASES_STATUS)) {
          statusFilterParameter = new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS,
            simpleFilterParameter.getValue());
        }
      }
    }
    statusFilter.add(statusFilterParameter);
    return new ArrayList<>(List.of(statusFilter));
  }

  private List<Filter> getDatabaseFindAllFilterQueries() {
    return new ArrayList<>(
      List.of(new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL, "true"))));
  }

  public static List<Filter> getDatabaseFindContentTypeFilterQueries() {
    Filter filter = new Filter();
    filter.add(
      new SimpleFilterParameter(ViewerConstants.SOLR_CONTENT_TYPE, ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_ROOT));
    return new ArrayList<>(List.of(filter));
  }

  public static List<Filter> getDatabaseFindUserPermissionsFilterQueries(User user) {
    Filter filter = new Filter();

    String zoneIdString = ViewerConfiguration.getInstance().getViewerConfigurationAsString("UTC",
      ViewerConstants.PROPERTY_EXPIRY_ZONE_ID_OVERRIDE);
    ZoneId zoneId;
    try {
      zoneId = ZoneId.of(zoneIdString);
    } catch (DateTimeException e) {
      zoneId = ZoneOffset.UTC;
    }
    // LocalDateTime gets the current time in the configured timezone...
    LocalDateTime nowDateTime = LocalDateTime.ofInstant(new Date().toInstant(), zoneId);
    // ... and then we convert to Date using UTC so that it is sent to the query with the timezone's offset
    Date now = Date.from(nowDateTime.atZone(ZoneOffset.UTC).toInstant());

    BlockJoinAnyParentExpiryFilterParameter param = new BlockJoinAnyParentExpiryFilterParameter(user.getAllRoles(), now,
      null);
    filter.add(param);
    return new ArrayList<>(List.of(filter));
  }

  @Override
  public ViewerDatabase retrieve(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      UserUtility.checkDatabasePermission(user, databaseUUID);
      return ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
    } catch (NotFoundException | GenericException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      UserUtility.checkDatabasePermission(user, databaseUUID);
      return SIARDController.deleteAll(databaseUUID);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }
  }

  @Override
  public Map<String, AuthorizationDetails> getDatabasePermissions(String databaseUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      UserUtility.checkDatabasePermission(user, databaseUUID);
      DatabaseStatus databaseStatus = ViewerFactory.getConfigurationManager().getDatabaseStatus(databaseUUID);
      return databaseStatus.getPermissions();
    } catch (GenericException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM, databaseUUID);
    }

  }

  @Override
  public Map<String, AuthorizationDetails> updateDatabasePermissions(String databaseUUID,
    Map<String, AuthorizationDetails> permissions) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      UserUtility.checkDatabasePermission(user, databaseUUID);
      return SIARDController.updateDatabasePermissions(databaseUUID, permissions);
    } catch (GenericException | ViewerException | AuthorizationException e) {
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
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      UserUtility.checkDatabasePermission(user, databaseUUID);
      return SIARDController.updateDatabaseSearchAllAvailability(databaseUUID);
    } catch (GenericException | ViewerException | NotFoundException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }

  }

}
