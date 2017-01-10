package com.databasepreservation.visualization.client;

import java.util.List;

import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchField;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * The client side stub for the browser service.
 */
@RemoteServiceRelativePath("browse")
public interface BrowserService extends RemoteService {
  /**
   * Service location
   */
  static final String SERVICE_URI = "browse";

  /**
   * Utilities
   */
  public static class Util {

    /**
     * @return the singleton instance
     */
    public static BrowserServiceAsync getInstance() {
      BrowserServiceAsync instance = (BrowserServiceAsync) GWT.create(BrowserService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  List<SearchField> getSearchFields(ViewerTable viewerTable) throws GenericException;

  <T extends IsIndexed> IndexResult<T> find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString) throws GenericException, AuthorizationDeniedException, RequestNotValidException;

  <T extends IsIndexed> Long count(String classNameToReturn, Filter filter) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException;

  <T extends IsIndexed> T retrieve(String classNameToReturn, String id) throws AuthorizationDeniedException,
    GenericException, NotFoundException;

  <T extends IsIndexed> IndexResult<T> findRows(String classNameToReturn, String tableUUID, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, String localeString) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException;

  <T extends IsIndexed> Long countRows(String classNameToReturn, String tableUUID, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  <T extends IsIndexed> T retrieveRows(String classNameToReturn, String tableUUID, String rowUUID)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  String getSolrQueryString(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  String saveSearch(String name, String description, String tableUUID, String tableName, String databaseUUID,
    SearchInfo searchInfo) throws AuthorizationDeniedException, GenericException, RequestNotValidException,
    NotFoundException;

  void editSearch(String savedSearchUUID, String name, String description) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, NotFoundException;

  void deleteSearch(String savedSearchUUID) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException;
}
