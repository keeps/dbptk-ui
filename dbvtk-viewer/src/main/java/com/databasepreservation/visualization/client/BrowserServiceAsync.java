package com.databasepreservation.visualization.client;

import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchField;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BrowserServiceAsync {
  /**
   * Utility class to get the RPC Async interface from client-side code
   */
  public static final class Util {
    private static BrowserServiceAsync instance;

    public static final BrowserServiceAsync getInstance() {
      if (instance == null) {
        instance = (BrowserServiceAsync) GWT.create(BrowserService.class);
      }
      return instance;
    }

    private Util() {
      // Utility class should not be instantiated
    }
  }

  // databases

  void findDatabases(Filter filter, Sorter sorter, Sublist sublist, Facets facets, java.lang.String localeString,
    AsyncCallback<org.roda.core.data.v2.index.IndexResult<ViewerDatabase>> callback);

  // saved searches

  void findSavedSearches(java.lang.String databaseUUID, Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    java.lang.String localeString, AsyncCallback<org.roda.core.data.v2.index.IndexResult<SavedSearch>> callback);

  // Any kind

  <T extends IsIndexed> void retrieve(String databaseUUID, java.lang.String classNameToReturn, java.lang.String id,
    AsyncCallback<T> callback);

  // rows

  <T extends IsIndexed> void findRows(java.lang.String databaseUUID, java.lang.String tableUUID, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, java.lang.String localeString,
    AsyncCallback<org.roda.core.data.v2.index.IndexResult<ViewerRow>> callback);

  void countRows(java.lang.String databaseUUID, java.lang.String tableUUID, Filter filter,
    AsyncCallback<java.lang.Long> callback);

  void retrieveRows(java.lang.String databaseUUID, java.lang.String tableUUID, java.lang.String rowUUID,
    AsyncCallback<ViewerRow> callback);

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
  public void getAuthenticatedUser(AsyncCallback<User> callback);

  /**
   * Login into RODA Core
   *
   * @param username
   * @param password
   * @return
   * @throws RODAException
   */
  public void login(String username, String password, AsyncCallback<User> callback);
}
