package com.databasepreservation.dbviewer.server;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.RodaUser;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.shared.FieldVerifier;
import com.databasepreservation.dbviewer.shared.ViewerFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.List;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {

  public String greetServer(String input) throws IllegalArgumentException {
    // Verify that the input is valid.
    if (!FieldVerifier.isValidName(input)) {
      // If the input is not valid, throw an IllegalArgumentException back to
      // the client.
      throw new IllegalArgumentException("Name must be at least 4 characters long");
    }

    String serverInfo = getServletContext().getServerInfo();
    String userAgent = getThreadLocalRequest().getHeader("User-Agent");

    // Escape data from the client to avoid cross-site script vulnerabilities.
    input = escapeHtml(input);
    userAgent = escapeHtml(userAgent);

    return "Hello, " + input + "!<br><br>I am running " + serverInfo + ".<br><br>It looks like you are using:<br>"
      + userAgent;
  }

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
  public <T extends IsIndexed> IndexResult<T> findRows(String classNameToReturn, String tableUUID, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, String localeString) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().findRows(user, classToReturn, tableUUID, filter, sorter, sublist, facets);
  }

  @Override
  public <T extends IsIndexed> Long countRows(String classNameToReturn, String tableUUID, Filter filter) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().countRows(user, classToReturn, tableUUID, filter);
  }

  @Override
  public <T extends IsIndexed> T retrieveRows(String classNameToReturn, String tableUUID, String rowUUID) throws AuthorizationDeniedException,
    GenericException, NotFoundException {
    RodaUser user = null;
    Class<T> classToReturn = parseClass(classNameToReturn);
    return ViewerFactory.getSolrManager().retrieveRows(user, classToReturn, tableUUID, rowUUID);
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
