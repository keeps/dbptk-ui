/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpMethod;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.ObjectPermission;
import org.roda.core.data.v2.common.ObjectPermissionResult;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.filter.CasClient;
import com.databasepreservation.visualization.server.ViewerConfiguration;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class UserUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);
  public static final String RODA_USER = "RODA_USER";

  private static LoadingCache<Pair<String, User>, Boolean> databasePermissions = CacheBuilder
    .newBuilder()
    .expireAfterWrite(
      ViewerConfiguration.getInstance().getViewerConfigurationAsInt(60,
        ViewerConfiguration.PROPERTY_AUTHORIZATION_CACHE_TTL), TimeUnit.SECONDS)
    .build(new DatabasePermissionsCacheLoader());

  /** Private empty constructor */
  private UserUtility() {

  }

  public static User getApiUser(final HttpServletRequest request) throws AuthorizationDeniedException {
    return getUser(request, false);
  }

  public static User getUser(final HttpServletRequest request, final boolean returnGuestIfNoUserInSession) {
    User user = (User) request.getSession().getAttribute(RODA_USER);
    if (user == null) {
      user = returnGuestIfNoUserInSession ? getGuest() : null;
    } else {
      if (user.isGuest()) {
        user = getGuest();
      }
    }
    return user;
  }

  public static User getUser(final HttpServletRequest request) {
    return getUser(request, true);
  }

  public static void setUser(final HttpServletRequest request, final User user) {
    request.getSession(true).setAttribute(RODA_USER, user);
  }

  public static void logout(HttpServletRequest servletRequest) {
    servletRequest.getSession().setAttribute(RODA_USER, getGuest());
    // CAS specific clean up
    servletRequest.getSession().removeAttribute("edu.yale.its.tp.cas.client.filter.user");
    servletRequest.getSession().removeAttribute("_const_cas_assertion_");
  }

  /**
   * Retrieves guest used
   */
  public static User getGuest() {
    return new User("guest", "guest", true);
  }

  private static boolean userCanAccessDatabase(User user, String databaseUUID) throws AuthorizationDeniedException,
    NotFoundException, GenericException {
    try {
      return databasePermissions.get(new Pair<>(databaseUUID, user));
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof AuthorizationDeniedException) {
        throw (AuthorizationDeniedException) cause;
      } else if (cause instanceof NotFoundException) {
        throw (NotFoundException) cause;
      } else if (cause instanceof GenericException) {
        throw (GenericException) cause;
      } else {
        throw new GenericException(e);
      }
    }
  }

  private static boolean userIsAdmin(User user) {
    return ViewerConfiguration.getInstance()
      .getViewerConfigurationAsList(ViewerConfiguration.PROPERTY_AUTHORIZATION_ADMINS).contains(user.getName());
  }

  private static boolean userIsManager(User user) {
    return ViewerConfiguration.getInstance()
      .getViewerConfigurationAsList(ViewerConfiguration.PROPERTY_AUTHORIZATION_MANAGERS).contains(user.getName());
  }

  private static boolean userIsAdminOrManager(User user) {
    return userIsAdmin(user) || userIsManager(user);
  }

  public static class Authorization {
    private static final Map<Class, String> filterParameterDatabaseUUID;

    static {
      filterParameterDatabaseUUID = new HashMap<>();
      filterParameterDatabaseUUID.put(SavedSearch.class, ViewerSafeConstants.SOLR_SEARCHES_DATABASE_UUID);
    }

    private static AuthorizationDeniedException error(User user, String objectType, String object) {
      return new AuthorizationDeniedException("Access to " + objectType + " '" + object + "' has been denied for user "
        + user.getName());
    }

    private static AuthorizationDeniedException error(User user, String objectType) {
      return new AuthorizationDeniedException("Access to " + objectType + " has been denied for user " + user.getName());
    }

    private static AuthorizationDeniedException errorAdmin(User user, String action) {
      return new AuthorizationDeniedException("Only administrators can " + action + ", and the current user("
        + user.getName() + ") is not an administrator.");
    }

    public static boolean isEnabled() {
      return ViewerConfiguration.getInstance().getViewerConfigurationAsBoolean(
        ViewerConfiguration.PROPERTY_AUTHORIZATION_ENABLED);
    }

    public static void allowIfAdmin(final HttpServletRequest request) throws AuthorizationDeniedException {
      if (!isEnabled()) {
        return;
      }
      User user = getUser(request);
      if (!userIsAdmin(user)) {
        throw errorAdmin(user, "list databases");
      }
    }

    public static void allowIfAdminOrManager(final HttpServletRequest request) throws AuthorizationDeniedException {
      if (!isEnabled()) {
        return;
      }
      User user = getUser(request);
      if (!userIsAdminOrManager(user)) {
        throw errorAdmin(user, "manage saves searches");
      }
    }

    public static void checkFilteringPermission(final HttpServletRequest request, String databaseUUID, Filter filter,
      Class returnClass) throws AuthorizationDeniedException, NotFoundException, GenericException {
      if (!isEnabled()) {
        return;
      }
      User user = getUser(request);
      if (userCanAccessDatabase(user, databaseUUID) && filterParameterDatabaseUUID.containsKey(returnClass)) {
        filter.add(new SimpleFilterParameter(filterParameterDatabaseUUID.get(returnClass), databaseUUID));
      } else {
        throw error(user, returnClass.getName());
      }
    }

    public static <T extends IsIndexed> void checkRetrievalPermission(HttpServletRequest request, String databaseUUID,
      Class<T> resultClass, T result) throws AuthorizationDeniedException, NotFoundException, GenericException {
      if (!isEnabled()) {
        return;
      }

      User user = getUser(request);
      if (resultClass.equals(ViewerDatabase.class) && userCanAccessDatabase(user, databaseUUID)) {
        // the user can access the database
        return;

      } else if (resultClass.equals(SavedSearch.class)) {
        // the user can access the database to which this saved search belongs
        SavedSearch savedSearch = (SavedSearch) result;
        if (databaseUUID.equals(savedSearch.getDatabaseUUID()) && userCanAccessDatabase(user, databaseUUID)) {
          return;
        }

      }
      // access to anything else is denied
      throw error(user, resultClass.getName(), result.getId());
    }

    public static void checkTableAccessPermission(HttpServletRequest request, ViewerDatabase database, String tableUUID)
      throws AuthorizationDeniedException, NotFoundException, GenericException {
      if (!isEnabled()) {
        return;
      }

      User user = getUser(request);
      // sanity check: table must belong to the database. needed to protect
      // against ViewerTable object forgery
      if (database.getMetadata().getTable(tableUUID) != null && userCanAccessDatabase(user, database.getUUID())) {
        // allow if the user can access the database that the table belongs to
        return;
      }

      // access to everything else is denied
      throw error(user, "table", tableUUID);
    }

    public static void checkSavedSearchPermission(HttpServletRequest request, String databaseUUID,
      SavedSearch savedSearch) throws AuthorizationDeniedException, NotFoundException, GenericException {
      if (!isEnabled()) {
        return;
      }

      User user = getUser(request);
      if (databaseUUID.equals(savedSearch.getDatabaseUUID()) && userCanAccessDatabase(user, databaseUUID)) {
        return;
      }

      throw error(user, "saved search", savedSearch.getUUID());
    }
  }

  private static class DatabasePermissionsCacheLoader extends CacheLoader<Pair<String, User>, Boolean> {

    /**
     * Computes or retrieves the value corresponding to the databaseUUID/user
     * {@code pair}.
     *
     * @param pair
     *          the non-null databaseUUID/user pair whose value should be loaded
     * @return the value associated with the databaseUUID/user {@code pair};
     *         <b>must not be null</b>
     * @throws Exception
     *           if unable to load the result
     * @throws InterruptedException
     *           if this method is interrupted. {@code InterruptedException} is
     *           treated like any other {@code Exception} in all respects except
     *           that, when it is caught, the thread's interrupt status is set
     */
    @Override
    public Boolean load(Pair<String, User> pair) throws Exception {
      final boolean usingCAS = ViewerConfiguration.getInstance().getViewerConfigurationAsBoolean(false,
        ViewerConfiguration.PROPERTY_FILTER_AUTHENTICATION_CAS);
      String databaseUUID = pair.getFirst();
      User user = pair.getSecond();

      String dbvtkUser = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConfiguration.PROPERTY_AUTHORIZATION_QUERY_USER_USERNAME);
      String dbvtkPass = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConfiguration.PROPERTY_AUTHORIZATION_QUERY_USER_PASSWORD);

      Client client;
      if (usingCAS) {
        client = ClientBuilder.newClient();
      } else {
        client = getBasicAuthClient(dbvtkUser, dbvtkPass);
      }

      String rodaAddress = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConfiguration.PROPERTY_RODA_ADDRESS);

      String userPermissionPath = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConfiguration.PROPERTY_AUTHORIZATION_QUERY_PATH);
      userPermissionPath = userPermissionPath.replaceAll("\\{username\\}", user.getName()).replaceAll(
        "\\{databaseUUID\\}", databaseUUID);

      String userPermissionParameters = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConfiguration.PROPERTY_AUTHORIZATION_QUERY_PARAMETERS);
      userPermissionParameters = userPermissionParameters.replaceAll("\\{username\\}", user.getName()).replaceAll(
        "\\{databaseUUID\\}", databaseUUID);

      UriBuilder uri = client.target(rodaAddress).path(userPermissionPath).getUriBuilder();
      uri.replaceQuery(userPermissionParameters);
      WebTarget target = client.target(uri);

      try {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        if (usingCAS) {
          addTokenGrantingToken(dbvtkUser, dbvtkPass, request);
        }
        String jsonObj = request.get(String.class);

        ObjectPermissionResult permissions = JsonUtils.getObjectFromJson(jsonObj, ObjectPermissionResult.class);

        boolean allowed = false;
        for (ObjectPermission objectPermission : permissions.getObjects()) {
          if (databaseUUID.equals(objectPermission.getObjectId()) && objectPermission.isHasPermission()) {
            allowed = true;
            break;
          }
        }

        return allowed;
      } catch (NotAuthorizedException e) {
        throw new AuthorizationDeniedException("Could not login with the provided DBVTK username and password", e);
      } catch (javax.ws.rs.NotFoundException e) {
        LOGGER.error("link {}/{}", rodaAddress, userPermissionPath);
        LOGGER.error("Could not find the specified DIP", e);
        throw new NotFoundException("Could not find the specified DIP", e);
      } catch (GenericException e) {
        throw new GenericException("Could not understand the server response", e);
      } catch (BadRequestException e) {
        String responseText = e.getResponse().readEntity(String.class);
        LOGGER.error("BadRequestException. Response: {}", responseText);
        throw e;
      }
    }

    /**
     * Creates a Client that includes basic auth credentials
     */
    private Client getBasicAuthClient(String username, String password) throws Exception {
      HttpAuthenticationFeature basicAuth = HttpAuthenticationFeature.basic(username, password);
      return ClientBuilder.newClient().register(basicAuth);
    }

    /**
     * Gets a CAS token using the user/pass, then modifies the request to
     * include the token
     */
    private Invocation.Builder addTokenGrantingToken(String username, String password, Invocation.Builder request)
      throws Exception {
      String token;

      final String casServerUrlPrefix = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConfiguration.PROPERTY_FILTER_AUTHENTICATION_CAS_SERVER_URL_PREFIX);
      final CasClient casClient = new CasClient(casServerUrlPrefix);
      try {
        token = casClient.getTicketGrantingTicket(username, password);
      } catch (final AuthenticationDeniedException e) {
        // added explicitly for readibility
        throw e;
      }

      return request.header("tgt", token);
    }

    /**
     * Returns an error message for invalid response from CAS server.
     *
     * @param method
     *          the HTTP method
     * @return a String with the error message.
     */
    private String invalidResponseMessage(final HttpMethod method) {
      return String.format("Invalid response from CAS server: %s - %s", method.getStatusCode(), method.getStatusText());
    }
  }
}
