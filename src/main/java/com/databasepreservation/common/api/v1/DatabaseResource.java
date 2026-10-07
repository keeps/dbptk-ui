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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.StreamResponse;
import com.databasepreservation.common.api.v1.utils.DatabasesCSVOutputStream;
import com.databasepreservation.common.api.v1.utils.ParameterSanitization;
import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
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
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.config.SearchContentConfig;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.UserUtility;
import com.databasepreservation.model.exception.ModuleException;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_DATABASE)
public class DatabaseResource implements DatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResource.class);
  @Autowired
  private HttpServletRequest request;

  @Autowired
  @Qualifier(SearchContentConfig.SEARCH_CONTENT_EXECUTOR_BEAN_NAME)
  private ThreadPoolTaskExecutor searchAllExecutor;

  @Autowired
  private SearchContentConfig searchContentConfig;

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
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA); // deprecated
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_NAME);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DESCRIPTION);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVER);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVER_CONTACT);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DATA_OWNER);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_ORIGIN_TIMESPAN);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_PRODUCER_APPLICATION);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVAL_DATE);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_CLIENT_MACHINE);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DATABASE_PRODUCT);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DATABASE_USER);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_PERMISSIONS);

        FindRequest userFindRequest = new FindRequest(findRequest.classToReturn, findRequest.filter, findRequest.sorter,
          findRequest.sublist, findRequest.facets, findRequest.exportFacets, fieldsToReturn,
          findRequest.extraParameters, findRequest.defType, new Filter(), findRequest.queryFields,
          findRequest.highlighting, List.of());
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
      if (e.getCause() != null) {
        LOGGER.error("Database creation failed: {}", e.getCause().toString());
      } else {
        LOGGER.error("Database creation failed: {}", e.toString());
      }
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
        findRequest.defType, filterQueries, findRequest.queryFields);
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
        findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets, fieldsToReturn,
        findRequest.defType, filterQueries, findRequest.queryFields);
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
    Filter filter = SolrUtils.removeIndexIdFromSearch(findRequest.filter);
    try {
      ArrayList<Filter> filterQueries = new ArrayList<>();
      filterQueries.addAll(getDatabaseFindContentTypeFilterQueries());
      filterQueries.addAll(getDatabaseFindAllFilterQueries());
      if (!user.isAdmin() && !user.isWhiteList()) {
        filterQueries.addAll(getDatabaseFindStatusFilterQueries(filter));
        filterQueries.addAll(getDatabaseFindUserPermissionsFilterQueries(user));
      }
      List<String> fieldsToReturn = findRequest.fieldsToReturn;
      if (filter != null) {
        fieldsToReturn = new ArrayList<>();
        fieldsToReturn.add(ViewerConstants.INDEX_ID);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_STATUS);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA); // deprecated
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_NAME);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DESCRIPTION);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DATA_OWNER);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_ARCHIVAL_DATE);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_METADATA_DATABASE_PRODUCT);
        fieldsToReturn.add(ViewerConstants.SOLR_DATABASES_PERMISSIONS);
      }

      // only search on the available databases
      Map<String, ViewerDatabase> databaseMap = new HashMap<>();
      try (IterableDatabaseResult<ViewerDatabase> databases = ViewerFactory.getSolrManager()
        .findAll(ViewerDatabase.class, new Filter(), Sorter.NONE, fieldsToReturn, filterQueries)) {
        for (ViewerDatabase database : databases) {
          if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)) {
            databaseMap.put(database.getUuid(), database);
          }
        }
      }

      if (databaseMap.isEmpty()) {
        return new IndexResult<>();
      }

      Map<String, Long> hitsPerDatabase = countHitsPerDatabase(databaseMap.keySet(), filter, findRequest.defType,
        findRequest.queryFields);

      // most hits first, as the previous facet sorting by count
      List<ViewerDatabase> databasesWithHits = new ArrayList<>();
      for (Map.Entry<String, Long> entry : hitsPerDatabase.entrySet()) {
        if (entry.getValue() > 0) {
          ViewerDatabase database = databaseMap.get(entry.getKey());
          database.setSearchHits(entry.getValue());
          databasesWithHits.add(database);
          count += entry.getValue();
        }
      }
      databasesWithHits.sort(
        Comparator.comparingLong(ViewerDatabase::getSearchHits).reversed().thenComparing(ViewerDatabase::getUuid));

      int offset = findRequest.sublist.getFirstElementIndex();
      int limit = findRequest.sublist.getMaximumElementCount();
      int fromIndex = Math.min(offset, databasesWithHits.size());
      int toIndex = Math.min(fromIndex + limit, databasesWithHits.size());

      IndexResult<ViewerDatabase> searchHitsResult = new IndexResult<>();
      searchHitsResult.setTotalCount(databasesWithHits.size());
      searchHitsResult.setLimit(limit);
      searchHitsResult.setOffset(offset);
      searchHitsResult.setResults(new ArrayList<>(databasesWithHits.subList(fromIndex, toIndex)));

      return searchHitsResult;
    } catch (GenericException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } catch (RuntimeException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(filter), ViewerConstants.CONTROLLER_SUBLIST_PARAM,
        JsonUtils.getJsonFromObject(findRequest.sublist), ViewerConstants.CONTROLLER_RETRIEVE_COUNT, count);
    }
  }

  private Map<String, Long> countHitsPerDatabase(Collection<String> databaseUUIDs, Filter filter, String defType,
    List<String> queryFields) throws GenericException {
    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    Map<String, Future<Long>> futures = new LinkedHashMap<>();
    for (String databaseUUID : databaseUUIDs) {
      futures.put(databaseUUID, searchAllExecutor.submit(() -> solrManager.countHits(databaseUUID, filter, defType,
        queryFields, searchContentConfig.getCollectionTimeAllowedMillis())));
    }

    Map<String, Long> hitsPerDatabase = new HashMap<>();
    int failed = 0;
    int timedOut = 0;
    Exception lastFailure = null;
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(searchContentConfig.getSearchTimeoutMillis());
    try {
      for (Map.Entry<String, Future<Long>> entry : futures.entrySet()) {
        try {
          long remaining = Math.max(0, deadline - System.nanoTime());
          hitsPerDatabase.put(entry.getKey(), entry.getValue().get(remaining, TimeUnit.NANOSECONDS));
        } catch (ExecutionException e) {
          failed++;
          lastFailure = e;
          LOGGER.warn("Search all failed on database {}: {}", entry.getKey(),
            e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (TimeoutException e) {
          timedOut++;
          entry.getValue().cancel(true);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GenericException("Search on all databases was interrupted", e);
    } finally {
      // do not leave queued queries running for a search that already ended
      futures.values().forEach(future -> future.cancel(true));
    }

    if (failed > 0 || timedOut > 0) {
      LOGGER.warn("Search all incomplete: {} of {} databases failed and {} did not finish within {} ms", failed,
        databaseUUIDs.size(), timedOut, searchContentConfig.getSearchTimeoutMillis());
    }
    if (hitsPerDatabase.isEmpty() && failed > 0) {
      throw new GenericException("Search failed on all " + failed + " databases", lastFailure);
    }
    return hitsPerDatabase;
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
    // ... and then we convert to Date using UTC so that it is sent to the query
    // with the timezone's offset
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
    } catch (ViewerException | RequestNotValidException | GenericException | NotFoundException | AuthorizationException
      | IllegalAccessException e) {
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
      ParameterSanitization.sanitizePath(databaseUUID, "Invalid database UUID");
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
      ParameterSanitization.sanitizePath(databaseUUID, "Invalid database UUID");
      UserUtility.checkDatabasePermission(user, databaseUUID);
      return SIARDController.updateDatabaseSearchAllAvailability(databaseUUID);
    } catch (GenericException | ViewerException | NotFoundException | AuthorizationException
      | IllegalArgumentException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }

  }

  @Override
  public boolean updateDatabaseLocation(String databaseUUID, String path) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      ParameterSanitization.sanitizePath(databaseUUID, "Invalid database UUID");
      UserUtility.checkDatabasePermission(user, databaseUUID);
      return SIARDController.updateDatabaseLocation(databaseUUID, path);
    } catch (GenericException | ViewerException | AuthorizationException | IllegalArgumentException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, databaseUUID, state, ViewerConstants.CONTROLLER_DATABASE_ID_PARAM,
        databaseUUID);
    }

  }

  @Override
  public StringResponse reindex() {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();
    try {
      user = controllerAssistant.checkRoles(request);
      return SIARDController.reindex();
    } catch (ModuleException | AuthorizationException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  private ResponseEntity<StreamingResponseBody> handleDatabasesCSVExport(DatabaseRowsSolrManager solrManager,
    FindRequest findRequest) throws IOException {
    List<Filter> filterQueries = getDatabaseFindContentTypeFilterQueries();

    try (IterableDatabaseResult<ViewerDatabase> allDatabases = solrManager.findAll(ViewerDatabase.class,
      findRequest.filter, findRequest.sorter, findRequest.fieldsToReturn, filterQueries)) {
      return ApiUtils.okResponse(new StreamResponse(new DatabasesCSVOutputStream(allDatabases, "databases.csv", ',')));
    }
  }

  @RequestMapping(path = "/export", method = RequestMethod.GET)
  @Operation(summary = "Exports list of loaded databases to CSV")
  public ResponseEntity<StreamingResponseBody> exportDatabases() {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    FindRequest findRequest = null;

    try {
      user = controllerAssistant.checkRoles(request);
      findRequest = new FindRequest(ViewerDatabase.class.getName(), new Filter(), new Sorter(), null, null);
      return handleDatabasesCSVExport(solrManager, findRequest);
    } catch (AuthorizationException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

}
