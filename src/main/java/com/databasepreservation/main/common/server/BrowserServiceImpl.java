package com.databasepreservation.main.common.server;

import java.util.HashMap;
import java.util.List;

import com.databasepreservation.main.common.shared.SIARDProgressData;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.server.controller.SIARDController;
import com.databasepreservation.main.common.server.controller.UserLoginController;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.common.search.SavedSearch;
import com.databasepreservation.main.common.shared.client.common.search.SearchField;
import com.databasepreservation.main.common.shared.client.common.search.SearchInfo;
import com.databasepreservation.main.common.shared.client.common.utils.BrowserServiceUtils;
import com.databasepreservation.main.common.utils.UserUtility;
import com.databasepreservation.main.common.utils.UserUtility.Authorization;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.TableAndColumnsParameters;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  /**
   * Escape an html string. Escaping data received from the client helps to
   * prevent cross-site script vulnerabilities.
   *
   * @param html
   *          the html string to escape
   * @return the escaped string
   */
  private String escapeHtml(String html) {
    if (html == null) {
      return null;
    }
    return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  public IndexResult<ViewerDatabase> findDatabases(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
                                                   String localeString) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Authorization.allowIfAdmin(getThreadLocalRequest());
    return ViewerFactory.getSolrManager().find(ViewerDatabase.class, filter, sorter, sublist, facets);
  }

  public IndexResult<SavedSearch> findSavedSearches(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
                                                    Facets facets, String localeString)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Authorization.checkFilteringPermission(getThreadLocalRequest(), databaseUUID, filter, SavedSearch.class);
    return ViewerFactory.getSolrManager().find(SavedSearch.class, filter, sorter, sublist, facets);
  }

  @Override
  public <T extends IsIndexed> T retrieve(String databaseUUID, String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Class<T> classToReturn = parseClass(classNameToReturn);
    T result = ViewerFactory.getSolrManager().retrieve(classToReturn, id);
    Authorization.checkRetrievalPermission(getThreadLocalRequest(), databaseUUID, classToReturn, result);
    return result;
  }

  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
                                         Facets facets, String localeString)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    try {
      Authorization.checkDatabaseAccessPermission(getThreadLocalRequest(), databaseUUID);
    } catch (NotFoundException e) {
      throw new RequestNotValidException("Invalid database UUID: " + databaseUUID, e);
    }

    return ViewerFactory.getSolrManager().findRows(databaseUUID, filter, sorter, sublist, facets);
  }

  @Override
  public Long countRows(String databaseUUID, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    try {
      Authorization.checkDatabaseAccessPermission(getThreadLocalRequest(), databaseUUID);
    } catch (NotFoundException e) {
      throw new RequestNotValidException("Invalid database UUID: " + databaseUUID, e);
    }

    return ViewerFactory.getSolrManager().countRows(databaseUUID, filter);
  }

  @Override
  public ViewerRow retrieveRows(String databaseUUID, String rowUUID)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    Authorization.checkDatabaseAccessPermission(getThreadLocalRequest(), databaseUUID);
    return ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowUUID);
  }

  @Override
  public String getSolrQueryString(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    // does not retrieve data from index => safe to ignore authorization
    return SolrUtils.getSolrQuery(filter, sorter, sublist, facets);
  }

  @Override
  public String saveSearch(String name, String description, String tableUUID, String tableName, String databaseUUID,
    SearchInfo searchInfo)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Authorization.checkDatabaseAccessPermission(getThreadLocalRequest(), databaseUUID);

    String searchInfoJson = JsonUtils.getJsonFromObject(searchInfo);

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setUUID(SolrUtils.randomUUID());
    savedSearch.setName(name);
    savedSearch.setDescription(description);
    savedSearch.setDatabaseUUID(databaseUUID);
    savedSearch.setTableUUID(tableUUID);
    savedSearch.setTableName(tableName);
    savedSearch.setSearchInfoJson(searchInfoJson);

    ViewerFactory.getSolrManager().addSavedSearch(savedSearch);

    return savedSearch.getUUID();
  }

  @Override
  public void editSearch(String databaseUUID, String savedSearchUUID, String name, String description)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    // get the saved search
    SavedSearch savedSearch = ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
    // authorise viewing the saved search
    Authorization.checkSavedSearchPermission(getThreadLocalRequest(), databaseUUID, savedSearch);
    // authorise editing the saved search
    Authorization.allowIfAdminOrManager(getThreadLocalRequest());

    ViewerFactory.getSolrManager().editSavedSearch(savedSearchUUID, name, description);
  }

  @Override
  public void deleteSearch(String databaseUUID, String savedSearchUUID)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    // get the saved search
    SavedSearch savedSearch = ViewerFactory.getSolrManager().retrieve(SavedSearch.class, savedSearchUUID);
    // authorise viewing the saved search
    Authorization.checkSavedSearchPermission(getThreadLocalRequest(), databaseUUID, savedSearch);
    // authorise editing the saved search
    Authorization.allowIfAdminOrManager(getThreadLocalRequest());

    ViewerFactory.getSolrManager().deleteSavedSearch(savedSearchUUID);
  }

  @Override
  public Boolean isAuthenticationEnabled() throws RODAException {
    return ViewerConfiguration.getInstance().getIsAuthenticationEnabled();
  }

  @Override
  public List<SearchField> getSearchFields(ViewerTable viewerTable) throws GenericException {
    // does not retrieve data from index => safe to ignore authorization
    return BrowserServiceUtils.getSearchFieldsFromTable(viewerTable);
  }

  @SuppressWarnings("unchecked")
  private <T extends IsIndexed> Class<T> parseClass(String classNameToReturn) throws GenericException {
    Class<T> classToReturn;
    try {
      classToReturn = (Class<T>) Class.forName(classNameToReturn);
    } catch (ClassNotFoundException e) {
      throw new GenericException("Could not find class " + classNameToReturn);
    }
    return classToReturn;
  }

  public User getAuthenticatedUser() throws RODAException {
    User user = UserUtility.getUser(this.getThreadLocalRequest());
    LOGGER.debug("Serving user {}", user);
    return user;
  }

  public User login(String username, String password) throws AuthenticationDeniedException, GenericException {
    User user = UserLoginController.login(username, password, this.getThreadLocalRequest());
    LOGGER.debug("Logged user {}", user);
    return user;
  }

  @Override
  public String uploadSIARD(String path) throws GenericException {
    return SIARDController.loadFromLocal(path);
  }

  @Override
  public ViewerDatabase uploadSIARDStatus(String databaseUUID)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    return retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID);
  }

  @Override
  public String getReport(String databaseUUID)
    throws GenericException, AuthorizationDeniedException, NotFoundException {
    ViewerDatabase dummy = new ViewerDatabase();
    dummy.setUUID(databaseUUID);
    Authorization.checkRetrievalPermission(getThreadLocalRequest(), databaseUUID, ViewerDatabase.class, dummy);
    return SIARDController.getReportFileContents(databaseUUID);
  }

  @Override
  public String getApplicationType() {
    return System.getProperty("env", "browse");
  }

  @Override
  public String uploadMetadataSIARD(String path) throws GenericException {
    return SIARDController.loadMetadataFromLocal(path);
  }

  @Override
  public String findSIARDFile(String path) throws GenericException, RequestNotValidException {
    return ViewerFactory.getSolrManager().findSIARDFile(path);
  }

  @Override
  public DBPTKModule getDatabaseImportModules() throws GenericException {
    return SIARDController.getDatabaseImportModules();
  }

  @Override
  public DBPTKModule getSIARDExportModule(String moduleName) throws GenericException {
    return SIARDController.getSIARDExportModule(moduleName);
  }

  @Override
  public DBPTKModule getSIARDExportModules() throws GenericException {
    return SIARDController.getSIARDExportModules();
  }

  @Override
  public ViewerMetadata getSchemaInformation(String databaseUUID, String moduleName, HashMap<String, String> values) throws GenericException {
    return SIARDController.getDatabaseMetadata(databaseUUID, moduleName, values);
  }

  @Override
  public boolean testConnection(String databaseUUID, String moduleName, HashMap<String, String> parameters) throws GenericException {
    return SIARDController.testConnection(databaseUUID, moduleName, parameters);
  }

  @Override
  public boolean createSIARD(String UUID, ConnectionParameters connectionParameters,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters,
    ExportOptionsParameters exportOptionsParameters, MetadataExportOptionsParameters metadataExportOptionsParameters)
    throws GenericException {
    return SIARDController.createSIARD(UUID, connectionParameters, tableAndColumnsParameters, customViewsParameters,
      exportOptionsParameters, metadataExportOptionsParameters);
  }

  @Override
  public String generateUUID() {
    return SolrUtils.randomUUID();
  }

  @Override
  public ViewerMetadata updateMetadataInformation(ViewerMetadata metadata, ViewerSIARDBundle bundleSiard,
    String databaseUUID, String path) throws GenericException {
    return SIARDController.updateMetadataInformation(metadata, bundleSiard, databaseUUID, path);
  }

  @Override
  public SIARDProgressData getProgressData(String uuid) {
    return SIARDProgressData.getInstance(uuid);
  }
}
