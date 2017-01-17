/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.utils;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserUtility.class);
  public static final String RODA_USER = "RODA_USER";

  private static LdapUtility LDAP_UTILITY;

  /** Private empty constructor */
  private UserUtility() {

  }

  public static LdapUtility getLdapUtility() {
    return LDAP_UTILITY;
  }

  public static void setLdapUtility(LdapUtility ldapUtility) {
    LDAP_UTILITY = ldapUtility;
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
}
