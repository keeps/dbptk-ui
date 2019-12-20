/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.controller.UserLoginController;
import com.databasepreservation.common.utils.UserUtility;

/**
 * CAS authentication filter for web requests.
 */
public class CasWebAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CasWebAuthFilter.class);

  /** URL to logout in CAS service. */
  private String casLogoutURL;

  /**
   * Default constructor.
   */
  public CasWebAuthFilter() {
    // do nothing
  }

  @Override
  public void init(final FilterConfig config) throws ServletException {
    casLogoutURL = config.getInitParameter("casServerLogoutUrl");
    LOGGER.info(getClass().getSimpleName() + " initialized ok");
  }

  /**
   * @see Filter#destroy()
   */
  @Override
  public void destroy() {
    // do nothing
  }

  /**
   * @param request
   *          the request.
   * @param response
   *          the response.
   * @param chain
   *          the filter chain.
   * @throws IOException
   *           if some I/O error occurs.
   * @throws ServletException
   *           if some error occurs.
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
    throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    final String url = httpRequest.getRequestURL().toString();
    final String requestURI = httpRequest.getRequestURI();
    final String service = httpRequest.getParameter("service");
    final String hash = httpRequest.getParameter("hash");
    final String locale = httpRequest.getParameter("locale");
    final String branding = httpRequest.getParameter("branding");

    LOGGER.debug("URL: {} ; Request URI: {} ; Service: {} ; Hash: {}, Locale: {}, Branding: {}", url, requestURI,
      service, hash, locale, branding);

    if (url.endsWith("/login")) {
      final StringBuilder b = new StringBuilder();
      b.append(httpRequest.getContextPath()).append("/");

      if (StringUtils.isNotBlank(locale) && StringUtils.isNotBlank(branding)) {
        b.append("?locale=").append(locale).append("&branding=").append(branding);
      } else if (StringUtils.isNotBlank(locale)) {
        b.append("?locale=").append(locale);
      } else if (StringUtils.isNotBlank(branding)) {
        b.append("?branding=").append(branding);
      }

      if (StringUtils.isNotBlank(hash)) {
        b.append("#").append(hash);
      }

      if (httpRequest.getUserPrincipal() != null) {
        UserLoginController.casLogin(httpRequest.getUserPrincipal().getName(), httpRequest);
      }

      httpResponse.sendRedirect(b.toString());

    } else if (url.endsWith("/logout")) {
      UserLoginController.logout(httpRequest);

      UserUtility.logout(httpRequest);

      final StringBuilder b = new StringBuilder();
      b.append(url, 0, url.indexOf("logout")).append("#");

      httpResponse.sendRedirect(CommonUtils.constructRedirectUrl(casLogoutURL, "service", b.toString(), false, false));

    } else {
      chain.doFilter(request, response);
    }

  }
}
