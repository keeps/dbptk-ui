/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.utils.UserUtility;

/**
 * Internal authentication filter for web requests.
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class InternalWebAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(InternalWebAuthFilter.class);

  @Override
  public void init(final FilterConfig config) throws ServletException {
    LOGGER.info("{} initialized ok", getClass().getSimpleName());
  }

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

    if ("/login".equals(requestURI)) {
      final StringBuilder b = new StringBuilder();
      b.append(httpRequest.getContextPath()).append("/");

      if (StringUtils.isNotBlank(locale) && StringUtils.isNotBlank(branding)) {
        b.append("?locale=").append(locale).append("&branding=").append(branding);
      } else if (StringUtils.isNotBlank(locale)) {
        b.append("?locale=").append(locale);
      } else if (StringUtils.isNotBlank(branding)) {
        b.append("?branding=").append(branding);
      }

      b.append("#login");

      if (StringUtils.isNotBlank(hash)) {
        b.append(HistoryManager.HISTORY_SEP).append(hash);
      }

      httpResponse.sendRedirect(b.toString());
    } else if ("/logout".equals(requestURI)) {
      UserUtility.logout(httpRequest);

      final StringBuilder b = new StringBuilder();
      b.append(httpRequest.getContextPath()).append("/").append("#");
      httpResponse.sendRedirect(b.toString());
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
