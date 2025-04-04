/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apereo.cas.client.util.AbstractCasFilter;
import org.apereo.cas.client.validation.Assertion;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.utils.UserUtility;

/**
 * CAS authentication filter for API requests.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CasApiAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CasApiAuthFilter.class);
  /** List of excluded URLs. */
  private final List<String> exclusions = new ArrayList<>();
  /** CAS client. */
  private CasClient casClient;

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    casClient = new CasClient(filterConfig.getInitParameter("casServerUrlPrefix"));

    final String exclusionsParam = filterConfig.getInitParameter("exclusions");
    if (StringUtils.isNotBlank(exclusionsParam)) {
      final String[] listOfExclusions = exclusionsParam.split(",");
      for (String exclusion : listOfExclusions) {
        this.exclusions.add(exclusion.trim());
      }
    }
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain filterChain) throws IOException, ServletException {

    final HttpServletRequest request = (HttpServletRequest) servletRequest;
    final HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (isRequestUrlExcluded(request)) {
      LOGGER.debug("Request is ignored.");
      filterChain.doFilter(request, response);
      return;
    }

    final HttpSession session = request.getSession(false);
    final Assertion assertion = session != null
      ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)
      : null;

    if (assertion != null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String tgt = request.getHeader("TGT");
      final Pair<String, String> credentials = new BasicAuthRequestWrapper(request).getCredentials();

      if (StringUtils.isNotBlank(tgt)) {
        // try to use TGT
        doFilterWithTGT(request, response, filterChain, tgt);
      } else if (credentials != null) {
        // TGT is blank. Try to use username and password
        doFilterWithCredentials(request, response, filterChain, credentials.getFirst(), credentials.getSecond());
      } else {
        // no credentials provided. check list of allowed management IPs
        try {
          UserUtility.Authorization.checkDatabaseManagementPermission(request);
          filterChain.doFilter(request, response);
        } catch (AuthorizationDeniedException e) {
          throw new AuthenticationDeniedException("Could not authenticate", e);
        }
      }
    } catch (final AuthenticationDeniedException e) {
      LOGGER.debug(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    } catch (final GenericException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }

  @Override
  public void destroy() {
  }

  private void doFilterWithTGT(final HttpServletRequest request, final HttpServletResponse response,
    final FilterChain filterChain, final String tgt) throws GenericException, IOException, ServletException {
    final String serviceUrl = constructServiceUrl(request);
    final String st = casClient.getServiceTicket(tgt, serviceUrl);
    filterChain.doFilter(new ServiceTicketRequestWrapper(request, st), response);
  }

  private void doFilterWithCredentials(final HttpServletRequest request, final HttpServletResponse response,
    final FilterChain filterChain, final String username, final String password)
    throws GenericException, IOException, ServletException, AuthenticationDeniedException {
    final String tgt = casClient.getTicketGrantingTicket(username, password);
    doFilterWithTGT(request, response, filterChain, tgt);
  }

  private String constructServiceUrl(final HttpServletRequest request) {
    return String.format("%s?%s", request.getRequestURL(), request.getQueryString());
  }

  /**
   * Is the requested path in the list of exclusions?
   *
   * @param request
   *          the request.
   *
   * @return <code>true</code> if it is excluded and <code>false</code> otherwise.
   */
  private boolean isRequestUrlExcluded(final HttpServletRequest request) {
    for (String exclusion : this.exclusions) {
      if (request.getPathInfo() != null && request.getPathInfo().matches(exclusion)) {
        return true;
      }
    }
    return false;
  }

  /**
   * A {@link HttpServletRequestWrapper} that adds a <code>ticket</code> query
   * string parameter.
   * 
   * @author Rui Castro <rui.castro@gmai.com>
   */
  private class ServiceTicketRequestWrapper extends HttpServletRequestWrapper {

    /**
     * CAS service ticket.
     */
    private final String serviceTicket;
    /**
     * The query string.
     */
    private String queryString = null;

    /**
     * Constructor.
     * 
     * @param request
     *          the HTTP request.
     * @param serviceTicket
     *          the CAS service ticket.
     */
    ServiceTicketRequestWrapper(final HttpServletRequest request, final String serviceTicket) {
      super(request);
      setAttribute("ticket", serviceTicket);
      this.serviceTicket = serviceTicket;
    }

    @Override
    public String getQueryString() {
      if (this.queryString == null) {
        String qs = super.getQueryString();
        if (StringUtils.isBlank(qs)) {
          qs = "";
        } else {
          qs = qs + "&";
        }
        this.queryString = String.format("%sticket=%s", qs, this.serviceTicket);
      }
      return this.queryString;
    }

    @Override
    public String getParameter(final String name) {
      if ("ticket".equals(name)) {
        return this.serviceTicket;
      } else {
        return super.getParameter(name);
      }
    }
  }
}
