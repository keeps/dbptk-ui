package com.databasepreservation.visualization.server;

import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.RodaUser;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.search.SearchField;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.databasepreservation.visualization.shared.BrowserServiceUtils;
import com.databasepreservation.visualization.shared.ViewerFactory;
import com.databasepreservation.visualization.utils.SolrUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {
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

  @Override
  public <T extends IsIndexed> IndexResult<T> find(String classNameToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, String localeString) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().find(user, classToReturn, filter, sorter, sublist, facets);
  }

  @Override
  public <T extends IsIndexed> Long count(String classNameToReturn, Filter filter) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().count(user, classToReturn, filter);
  }

  @Override
  public <T extends IsIndexed> T retrieve(String classNameToReturn, String id) throws AuthorizationDeniedException,
    GenericException, NotFoundException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().retrieve(user, classToReturn, id);
  }

  @Override
  public <T extends IsIndexed> IndexResult<T> findRows(String classNameToReturn, String tableUUID, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, String localeString) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().findRows(user, classToReturn, tableUUID, filter, sorter, sublist, facets);
  }

  @Override
  public <T extends IsIndexed> Long countRows(String classNameToReturn, String tableUUID, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().countRows(user, classToReturn, tableUUID, filter);
  }

  @Override
  public <T extends IsIndexed> T retrieveRows(String classNameToReturn, String tableUUID, String rowUUID)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().retrieveRows(user, classToReturn, tableUUID, rowUUID);
  }

  @Override
  public String getSolrQueryString(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    return SolrUtils.getSolrQuery(filter, sorter, sublist, facets);
  }

  @Override
  public String saveSearch(String name, String description, String tableUUID, String tableName, String databaseUUID,
    SearchInfo searchInfo) throws AuthorizationDeniedException, GenericException, RequestNotValidException,
    NotFoundException {
    RodaUser user = null;
    String searchInfoJson = JsonUtils.getJsonFromObject(searchInfo);

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setUUID(SolrUtils.randomUUID());
    savedSearch.setName(name);
    savedSearch.setDescription(description);
    savedSearch.setDatabaseUUID(databaseUUID);
    savedSearch.setTableUUID(tableUUID);
    savedSearch.setTableName(tableName);
    savedSearch.setSearchInfoJson(searchInfoJson);

    ViewerFactory.getSolrManager().addSavedSearch(user, savedSearch);

    return savedSearch.getUUID();
  }

  @Override
  public void editSearch(String savedSearchUUID, String name, String description) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = null;

    ViewerFactory.getSolrManager().editSavedSearch(user, savedSearchUUID, name, description);
  }

  @Override
  public void deleteSearch(String savedSearchUUID) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException {
    RodaUser user = null;

    ViewerFactory.getSolrManager().deleteSavedSearch(user, savedSearchUUID);
  }

  @Override
  public List<SearchField> getSearchFields(ViewerTable viewerTable) throws GenericException {
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
}
