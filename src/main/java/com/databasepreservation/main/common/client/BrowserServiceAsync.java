package com.databasepreservation.main.common.client;

import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.main.common.shared.ProgressData;
import com.databasepreservation.main.common.shared.ValidationProgressData;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.common.search.SavedSearch;
import com.databasepreservation.main.common.shared.client.common.search.SearchField;
import com.databasepreservation.main.common.shared.client.common.search.SearchInfo;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.TableAndColumnsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BrowserServiceAsync {
  /**
   * Utility class to get the RPC Async interface from client-side code
   */
  final class Util {
    private static BrowserServiceAsync instance;

    public static final BrowserServiceAsync getInstance() {
      if (instance == null) {
        instance = GWT.create(BrowserService.class);
      }
      return instance;
    }

    private Util() {
      // Utility class should not be instantiated
    }
  }

  // databases

  void findDatabases(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString,
    AsyncCallback<org.roda.core.data.v2.index.IndexResult<ViewerDatabase>> callback);

  // saved searches

  void findSavedSearches(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString, AsyncCallback<org.roda.core.data.v2.index.IndexResult<SavedSearch>> callback);

  // Any kind

  <T extends IsIndexed> void retrieve(String databaseUUID, String classNameToReturn, String id,
    AsyncCallback<T> callback);

  // rows

  <T extends IsIndexed> void findRows(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString, AsyncCallback<org.roda.core.data.v2.index.IndexResult<ViewerRow>> callback);

  void countRows(String databaseUUID, Filter filter, AsyncCallback<Long> callback);

  void retrieveRows(String databaseUUID, String rowUUID, AsyncCallback<ViewerRow> callback);

  void getSearchFields(ViewerTable viewerTable, AsyncCallback<List<SearchField>> async);

  void getSolrQueryString(Filter filter, Sorter sorter, Sublist sublist, Facets facets, AsyncCallback<String> async)
    throws GenericException, RequestNotValidException;

  void saveSearch(String name, String description, String tableUUID, String tableName, String databaseUUID,
    SearchInfo searchInfo, AsyncCallback<String> async);

  void editSearch(String databaseUUID, String savedSearchUUID, String name, String description,
    AsyncCallback<Void> async);

  void deleteSearch(String databaseUUID, String savedSearchUUID, AsyncCallback<Void> async);

  void isAuthenticationEnabled(AsyncCallback<Boolean> async);

  /**
   * Get the authenticated user
   *
   * @return
   * @throws RODAException
   */
  void getAuthenticatedUser(AsyncCallback<User> callback);

  /**
   * Login into RODA Core
   *
   * @param username
   * @param password
   * @return
   * @throws RODAException
   */
  void login(String username, String password, AsyncCallback<User> callback);

  void uploadSIARD(String path, AsyncCallback<String> async);

  void uploadSIARDStatus(String databaseUUID, AsyncCallback<ViewerDatabase> async);

  void getReport(String databaseUUID, AsyncCallback<String> async);

  void getApplicationType(AsyncCallback<String> async);

  void getClientMachine(AsyncCallback<String> async);

  void uploadMetadataSIARD(String databaseUUID, String path, AsyncCallback<String> async);

  void findSIARDFile(String path, AsyncCallback<String> async);

  void getDatabaseImportModules(AsyncCallback<DBPTKModule> async);

  void getDatabaseExportModules(AsyncCallback<DBPTKModule> async);

  void getSIARDExportModules(AsyncCallback<DBPTKModule> async);

  void getSIARDExportModule(String moduleName, AsyncCallback<DBPTKModule> async);

  void getSchemaInformation(String databaseUUID, ConnectionParameters values, AsyncCallback<ViewerMetadata> async);

  void testConnection(String databaseUUID, ConnectionParameters parameters, AsyncCallback<Boolean> async);

  void validateCustomViewQuery(String databaseUUID, ConnectionParameters parameters, String query, AsyncCallback<List<List<String>>> async);

  void createSIARD(String UUID, ConnectionParameters connectionParameters, TableAndColumnsParameters tableAndColumnsParameters,
    CustomViewsParameters customViewsParameters, ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters, AsyncCallback<Boolean> async);

  void migrateToDBMS(String databaseUUID, String siard, ConnectionParameters connectionParameters,
    AsyncCallback<Boolean> async);

  void migrateToSIARD(String databaseUUID, String siardPath, TableAndColumnsParameters tableAndColumnsParameters,
    ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters, AsyncCallback<Boolean> async);

  void generateUUID(AsyncCallback<String> async);

  void updateMetadataInformation(ViewerMetadata metadata, ViewerSIARDBundle bundleSiard, String databaseUUID,
    String path, AsyncCallback<ViewerMetadata> async);

  void validateSIARD(String databaseUUID, String SIARDPath, String validationReportPath, String allowedTypePath, AsyncCallback<Boolean> async);

  void getProgressData(String uuid, AsyncCallback<ProgressData> async);

  void getValidationProgressData(String uuid, AsyncCallback<ValidationProgressData> async);

  void getDateTimeHumanized(String dateTime, AsyncCallback<String> async);
}
