/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.common.search.SavedSearch;

public class UserUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);
  public static final String RODA_USER_NAME = "RODA_USER";
  public static final String RODA_USER_PERMISSIONS = "RODA_USER_PERMISSIONS";
  public static final String RODA_USER_PASS = "RODA_USER_PASS";

  /** Private empty constructor */
  private UserUtility() {

  }

  private static String getPasswordOrTicket(final HttpServletRequest request, User user, String databaseUUID)
    throws AuthorizationDeniedException {
    final boolean usingCAS = ViewerConfiguration.getInstance().getViewerConfigurationAsBoolean(false,
      ViewerConfiguration.PROPERTY_FILTER_AUTHENTICATION_CAS);

    if (user.isGuest()) {
      return StringUtils.EMPTY;
    }

    if (usingCAS) {
      String proxyTicketForRODA = null;
      String errorReason = null;

      HttpSession session = request.getSession();
      if (session != null) {
        Object attribute = session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        if (attribute instanceof Assertion) {
          Assertion assertion = (Assertion) attribute;
          UriBuilder dipUri = getDIPUri(databaseUUID);
          proxyTicketForRODA = assertion.getPrincipal().getProxyTicketFor(dipUri.toString());
        } else {
          errorReason = "Can not create a proxy ticket. Reason: CAS assertion is invalid";
        }
      } else {
        errorReason = "Can not create a proxy ticket. Reason: no session found";
      }

      if (StringUtils.isNotBlank(proxyTicketForRODA)) {
        return proxyTicketForRODA;
      } else {
        throw new AuthorizationDeniedException(errorReason);
      }
    } else {
      return (String) request.getSession().getAttribute(RODA_USER_PASS);
    }
  }

  public static User getUser(final HttpServletRequest request, final boolean returnGuestIfNoUserInSession) {
    User user = (User) request.getSession().getAttribute(RODA_USER_NAME);
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
    LOGGER.debug("Setting user: {}", user);
    request.getSession(true).setAttribute(RODA_USER_NAME, user);
    // do not keep old password after setting a new user
    request.getSession().removeAttribute(RODA_USER_PASS);
  }

  public static void setPassword(final HttpServletRequest request, final String password) {
    request.getSession(true).setAttribute(RODA_USER_PASS, password);
  }

  public static void logout(HttpServletRequest servletRequest) {
    LOGGER.debug("Removing user: {}", getUser(servletRequest, false));
    servletRequest.getSession().removeAttribute(RODA_USER_NAME);
    servletRequest.getSession().removeAttribute(RODA_USER_PERMISSIONS);
    servletRequest.getSession().removeAttribute(RODA_USER_PASS);
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

  private static boolean userCanAccessDatabase(final HttpServletRequest request, User user, String databaseUUID)
    throws AuthorizationDeniedException {

    // get current permissions object from session or create a new one
    DatabasePermissions permissions = (DatabasePermissions) request.getSession().getAttribute(RODA_USER_PERMISSIONS);
    if (permissions == null) {
      permissions = new DatabasePermissions(user);
    }

    // get permissions from RODA or use permission value from session
    Boolean canAccess = permissions.canAccessDatabase(user, databaseUUID,
      getPasswordOrTicket(request, user, databaseUUID));
    request.getSession().setAttribute(RODA_USER_PERMISSIONS, permissions);
    return canAccess;
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
      filterParameterDatabaseUUID.put(SavedSearch.class, ViewerConstants.SOLR_SEARCHES_DATABASE_UUID);
    }

    private static AuthorizationDeniedException error(User user, String objectType, String object) {
      return new AuthorizationDeniedException(
        "Access to " + objectType + " '" + object + "' has been denied for user " + user.getName());
    }

    private static AuthorizationDeniedException error(User user, String objectType) {
      return new AuthorizationDeniedException(
        "Access to " + objectType + " has been denied for user " + user.getName());
    }

    private static AuthorizationDeniedException errorAdmin(User user, String action) {
      return new AuthorizationDeniedException("Only administrators can " + action + ", and the current user("
        + user.getName() + ") is not an administrator.");
    }

    public static boolean isEnabled() {
      return ViewerConfiguration.getInstance()
        .getViewerConfigurationAsBoolean(ViewerConfiguration.PROPERTY_AUTHORIZATION_ENABLED);
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
      if (userCanAccessDatabase(request, user, databaseUUID) && filterParameterDatabaseUUID.containsKey(returnClass)) {
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
      if (resultClass.equals(ViewerDatabase.class) && userCanAccessDatabase(request, user, databaseUUID)) {
        // the user can access the database
        return;

      } else if (resultClass.equals(SavedSearch.class)) {
        // the user can access the database to which this saved search belongs
        SavedSearch savedSearch = (SavedSearch) result;
        if (databaseUUID.equals(savedSearch.getDatabaseUUID()) && userCanAccessDatabase(request, user, databaseUUID)) {
          return;
        }

      }
      // access to anything else is denied
      throw error(user, resultClass.getName(), result.getUuid());
    }

    public static void checkDatabaseAccessPermission(HttpServletRequest request, String databaseUUID)
      throws AuthorizationDeniedException, NotFoundException, GenericException {
      if (!isEnabled()) {
        return;
      }

      User user = getUser(request);
      // sanity check: table must belong to the database. needed to protect
      // against ViewerTable object forgery
      if (userCanAccessDatabase(request, user, databaseUUID)) {
        // allow if the user can access the database that the table belongs to
        return;
      }

      // access to everything else is denied
      throw error(user, "database", databaseUUID);
    }

    public static void checkSavedSearchPermission(HttpServletRequest request, String databaseUUID,
      SavedSearch savedSearch) throws AuthorizationDeniedException, NotFoundException, GenericException {
      if (!isEnabled()) {
        return;
      }

      User user = getUser(request);
      if (databaseUUID.equals(savedSearch.getDatabaseUUID()) && userCanAccessDatabase(request, user, databaseUUID)) {
        return;
      }

      throw error(user, "saved search", savedSearch.getUuid());
    }

    public static void checkDatabaseManagementPermission(HttpServletRequest request)
      throws AuthorizationDeniedException {

      String originIP = request.getRemoteAddr();
      if (ViewerConfiguration.getInstance().getWhitelistAllIPs()) {
        return;
      } else {
        try {
          InetAddress address = InetAddress.getByName(originIP);
          List<String> whitelistedIPs = ViewerConfiguration.getInstance().getWhitelistedIPs();
          for (String whitelistedIP : whitelistedIPs) {
            try {
              InetAddress whitelistAddress = InetAddress.getByName(whitelistedIP);
              if (Arrays.equals(address.getAddress(), whitelistAddress.getAddress())) {
                return;
              }
            } catch (UnknownHostException e) {
              LOGGER.debug("Invalid IP address from config: {}", originIP, e);
            }
          }
        } catch (UnknownHostException e) {
          LOGGER.debug("Invalid IP address: {}", originIP, e);
        }
      }

      // database removal request has been denied
      throw new AuthorizationDeniedException("Removal of database has been denied for address '" + originIP + "'.");
    }
  }

  private static UriBuilder getDIPUri(String databaseUUID) {
    return getDIPUri(null, databaseUUID, true);
  }

  private static UriBuilder getDIPUri(Client client, String databaseUUID) {
    return getDIPUri(client, databaseUUID, false);
  }

  private static UriBuilder getDIPUri(Client client, String databaseUUID, boolean useRodaCasServiceServerName) {
    boolean usingTemporaryClient = (client == null);
    if (usingTemporaryClient) {
      client = ClientBuilder.newClient();
    }

    String rodaAddress;
    if (useRodaCasServiceServerName) {
      rodaAddress = ViewerConfiguration.getInstance().getViewerConfigurationAsString(StringUtils.EMPTY,
        ViewerConfiguration.PROPERTY_AUTHORIZATION_RODA_CAS_SERVICE_NAME);
    } else {
      rodaAddress = ViewerConfiguration.getInstance().getViewerConfigurationAsString(StringUtils.EMPTY,
        ViewerConfiguration.PROPERTY_AUTHORIZATION_RODA_DIP_SERVER);
    }
    String rodaDipPath = ViewerConfiguration.getInstance().getViewerConfigurationAsString(StringUtils.EMPTY,
      ViewerConfiguration.PROPERTY_AUTHORIZATION_RODA_DIP_PATH);
    rodaDipPath = rodaDipPath.replaceAll("\\{dip_id\\}", databaseUUID);

    UriBuilder uri = client.target(rodaAddress).path(rodaDipPath).getUriBuilder();
    uri.queryParam("acceptFormat", "json");

    if (usingTemporaryClient) {
      client.close();
    }

    return uri;
  }

  private static class DatabasePermissions implements Serializable {
    private Map<String, Boolean> permissions = new HashMap<>();
    private User user;

    public DatabasePermissions(User user) {
      this.user = user;
    }

    private void invalidateCacheIfNeeded(User user) {
      // invalidate cache if we get a different user
      if (!StringUtils.equals(user.getName(), this.user.getName())) {
        LOGGER.debug("Session had user '{}' and now it has '{}', clearing permissions cache.", this.user.getName(),
          user.getName());
        permissions.clear();
        this.user = user;
      }
    }

    Boolean canAccessDatabase(User user, String databaseUUID, String ticketOrPassword) {
      invalidateCacheIfNeeded(user);

      Boolean cachedPermission = permissions.get(databaseUUID);
      if (cachedPermission != null) {
        LOGGER.debug("Using cached '{}' permission for user '{}' and database '{}'", cachedPermission, user.getName(),
          databaseUUID);
        return cachedPermission;
      }

      if (ticketOrPassword == null) {
        ticketOrPassword = StringUtils.EMPTY;
      }

      final boolean usingCAS = ViewerConfiguration.getInstance().getViewerConfigurationAsBoolean(false,
        ViewerConfiguration.PROPERTY_FILTER_AUTHENTICATION_CAS);

      Client client;
      if (!user.isGuest()) {
        if (usingCAS) {
          client = ClientBuilder.newClient();
        } else {
          client = getBasicAuthClient(user.getName(), ticketOrPassword);
        }
      } else {
        String rodaGuestUsername = ViewerConfiguration.getInstance().getViewerConfigurationAsString(null,
          ViewerConfiguration.PROPERTY_AUTHORIZATION_GUEST_USERNAME);
        String rodaGuestPassword = ViewerConfiguration.getInstance().getViewerConfigurationAsString(null,
          ViewerConfiguration.PROPERTY_AUTHORIZATION_GUEST_PASSWORD);
        client = getBasicAuthClient(rodaGuestUsername, rodaGuestPassword);
      }

      UriBuilder uri = getDIPUri(client, databaseUUID);
      if (usingCAS) {
        uri.queryParam("ticket", ticketOrPassword);
      }
      WebTarget target = client.target(uri);

      LOGGER.debug("URI: {}", uri);
      LOGGER.debug("ticketOrPassword: {}", ticketOrPassword);

      try {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = request.get();
        LOGGER.debug("STATUS: {}, HEADERS: {}", response.getStatus(), response.getStringHeaders());

        String jsonObj = response.readEntity(String.class);
        LOGGER.debug("jsonObj: {}", jsonObj);

        if (response.getStatus() == 200) {

          DIP dip = JsonUtils.getObjectFromJson(jsonObj, DIP.class);
          boolean hasPermission = databaseUUID.equals(dip.getId());
          permissions.put(databaseUUID, hasPermission);
          return hasPermission;
        } else if (response.getStatus() == 404) {
          LOGGER.debug("Could not find the specified DIP: {}", uri);
        } else if (response.getStatus() == 401 || response.getStatus() == 403) {
          LOGGER.debug("The user does not have permission to access the specified DIP: {}", uri);
        }
      } catch (NotAuthorizedException e) {
        // do nothing, false will be returned
      } catch (javax.ws.rs.NotFoundException e) {
        LOGGER.debug("Could not find the specified DIP: {}", uri);
      } catch (BadRequestException e) {
        String responseText = e.getResponse().readEntity(String.class);
        LOGGER.error("BadRequestException. Response: {}", responseText);
      } catch (ProcessingException | WebApplicationException e) {
        LOGGER.error("ProcessingException | WebApplicationException", e);
      } catch (GenericException e) {
        LOGGER.error("GenericException (json processing)", e);
      }

      client.close();
      permissions.put(databaseUUID, false);
      return false;
    }

    /**
     * Creates a Client that includes basic auth credentials
     */
    private Client getBasicAuthClient(String username, String password) {
      HttpAuthenticationFeature basicAuth = HttpAuthenticationFeature.basic(username, password);
      return ClientBuilder.newClient().register(basicAuth);
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
