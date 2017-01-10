package com.databasepreservation.visualization.client;

import java.util.List;

import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchField;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BrowserServiceAsync {

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.client.BrowserService
   */
  <T extends IsIndexed> void find(java.lang.String classNameToReturn, Filter filter,
    Sorter sorter, Sublist sublist,
    Facets facets, java.lang.String localeString,
    AsyncCallback<org.roda.core.data.v2.index.IndexResult<T>> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.client.BrowserService
   */
  void count(java.lang.String classNameToReturn, Filter filter,
    AsyncCallback<java.lang.Long> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.client.BrowserService
   */
  <T extends IsIndexed> void retrieve(java.lang.String classNameToReturn, java.lang.String id, AsyncCallback<T> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.client.BrowserService
   */
  <T extends IsIndexed> void findRows(java.lang.String classNameToReturn, java.lang.String tableUUID,
    Filter filter, Sorter sorter,
    Sublist sublist, Facets facets,
    java.lang.String localeString, AsyncCallback<org.roda.core.data.v2.index.IndexResult<T>> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.client.BrowserService
   */
  void countRows(java.lang.String classNameToReturn, java.lang.String tableUUID,
    Filter filter, AsyncCallback<java.lang.Long> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.client.BrowserService
   */
  <T extends IsIndexed> void retrieveRows(java.lang.String classNameToReturn, java.lang.String tableUUID,
    java.lang.String rowUUID, AsyncCallback<T> callback);

  void getSearchFields(ViewerTable viewerTable, AsyncCallback<List<SearchField>> async);

  void getSolrQueryString(Filter filter, Sorter sorter, Sublist sublist, Facets facets, AsyncCallback<String> async)
    throws GenericException, RequestNotValidException;

  void saveSearch(String name, String description, String tableUUID, String tableName, String databaseUUID,
    SearchInfo searchInfo, AsyncCallback<String> async);

  void editSearch(String savedSearchUUID, String name, String description, AsyncCallback<Void> async);

  void deleteSearch(String savedSearchUUID, AsyncCallback<Void> async);

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
}
