/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * A filter that can be turned on/off using RODA configuration file.
 */
public class OnOffFilter implements Filter {
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OnOffFilter.class);
  /**
   * Inner filter class parameter name.
   */
  private static final String PARAM_INNER_FILTER_CLASS = "inner-filter-class";
  /**
   * Configuration values prefix parameter name.
   */
  private static final String PARAM_CONFIG_PREFIX = "config-prefix";
  /**
   * Inner filter to which all calls to this filter will be delegated.
   */
  private Filter innerFilter = null;
  /**
   * This filter is ON?
   */
  private Boolean isOn = null;
  /**
   * The filter configuration from web.xml.
   */
  private FilterConfig webXmlFilterConfig = null;
  /**
   * Combined filter config.
   */
  private OnOffFilterConfig filterConfig = null;
  /**
   * IP addresses that are allowed to skip the filters when accessing some
   * resources
   */
  private List<String> whitelistedIPs;
  /**
   * Ignore the whitelist and allow all IPs to access protected resources
   */
  private boolean whitelistAllIPs;

  @Override
  @SuppressWarnings("checkstyle:hiddenfield")
  public void init(final FilterConfig filterConfig) throws ServletException {
    this.webXmlFilterConfig = filterConfig;
    if (isConfigAvailable()) {
      initInnerFilter();
    }
    whitelistedIPs = ViewerConfiguration.getInstance().getWhitelistedIPs();
    whitelistAllIPs = ViewerConfiguration.getInstance().getWhitelistAllIPs();
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain filterChain) throws IOException, ServletException {
    if (isOn() && !shouldSkipFilters(servletRequest)) {
      this.innerFilter.doFilter(servletRequest, servletResponse, filterChain);
    } else {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  /**
   * Checks if the request is for a resource that is not protected by
   * authentication filters but is instead protected via IP whitelist. And also if
   * the IP where the request is coming from is in the IP list.
   *
   * If both of those are true, this will return true (which will in turn skip all
   * filters).
   *
   * @param servletRequest
   *          the HTTP request
   *
   * @return true if the filters should be skipped
   */
  private boolean shouldSkipFilters(final ServletRequest servletRequest) {
    if (servletRequest instanceof HttpServletRequest) {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      String requestURI = request.getRequestURI();
      if (StringUtils.isNotBlank(requestURI)
        && requestURI.startsWith("/" + ViewerConstants.API_SERVLET + ViewerConstants.API_V1_MANAGE_RESOURCE)) {
        String remoteIP = request.getRemoteAddr();
        return whitelistAllIPs || whitelistedIPs.contains(remoteIP);
      }
    }

    return false;
  }

  @Override
  public void destroy() {
    if (this.innerFilter != null) {
      this.innerFilter.destroy();
    }
  }

  /**
   * Is this filter on?
   *
   * @return <code>true</code> if the Filter is on and <code>false</code>
   *         otherwise.
   * @throws ServletException
   *           if some error occurs.
   */
  private boolean isOn() throws ServletException {
    if (this.isOn == null && isConfigAvailable()) {
      initInnerFilter();
    }
    return this.isOn != null && this.isOn;
  }

  /**
   * Is RODA configuration available?
   *
   * @return <code>true</code> if RODA configuration is available and
   *         <code>false</code> otherwise.
   */
  private boolean isConfigAvailable() {
    return ViewerFactory.getViewerConfiguration() != null
      && ViewerFactory.getViewerConfiguration().getConfiguration() != null;
  }

  /**
   * Init inner filter.
   *
   * @throws ServletException
   *           if some error occurs.
   */
  private void initInnerFilter() throws ServletException {
    final Configuration rodaConfig = ViewerFactory.getViewerConfiguration().getConfiguration();
    if (rodaConfig == null) {
      LOGGER.info("DBVTK configuration not available yet. Delaying init of "
        + this.webXmlFilterConfig.getInitParameter(PARAM_INNER_FILTER_CLASS) + ".");
    } else {
      final String configPrefix = this.webXmlFilterConfig.getInitParameter(PARAM_CONFIG_PREFIX);
      this.isOn = rodaConfig.getBoolean(configPrefix + ".enabled", false);
      final String innerFilterClass = rodaConfig.getString(
        configPrefix + "." + this.webXmlFilterConfig.getFilterName() + "." + PARAM_INNER_FILTER_CLASS,
        this.webXmlFilterConfig.getInitParameter(PARAM_INNER_FILTER_CLASS));
      LOGGER.info(getFilterConfig().getFilterName() + " is " + (this.isOn ? "ON" : "OFF"));
      if (this.isOn) {
        try {
          this.innerFilter = (Filter) Class.forName(innerFilterClass).newInstance();
          this.innerFilter.init(getFilterConfig());
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
          throw new ServletException("Error instantiating inner filter - " + e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Return the combined filter configuration.
   *
   * @return the combined filter configuration.
   */
  private FilterConfig getFilterConfig() {
    if (filterConfig == null) {
      filterConfig = new OnOffFilterConfig(this.webXmlFilterConfig,
        ViewerFactory.getViewerConfiguration().getConfiguration());
    }
    return filterConfig;
  }

  /**
   * {@link FilterConfig} implementation that combines web.xml &lt;init-param> and
   * RODA configuration values.
   */
  private class OnOffFilterConfig implements FilterConfig {
    /**
     * Default {@link FilterConfig} (from web.xml).
     */
    private final FilterConfig filterConfig;
    /**
     * RODA configuration.
     */
    private final Configuration rodaConfig;
    /**
     * RODA configuration prefix for this filter.
     */
    private final String rodaConfigPrefix;
    /**
     * The list of init parameter names.
     */
    private List<String> configNames;

    /**
     * Constructor.
     *
     * @param filterConfig
     *          default filter configuration (from web.xml).
     * @param rodaConfig
     *          RODA configuration.
     */
    OnOffFilterConfig(final FilterConfig filterConfig, final Configuration rodaConfig) {
      this.filterConfig = filterConfig;
      this.rodaConfig = rodaConfig;
      final String configPrefix = this.filterConfig.getInitParameter(PARAM_CONFIG_PREFIX);
      if (StringUtils.isBlank(configPrefix)) {
        this.rodaConfigPrefix = String.format("ui.filter.%s", this.filterConfig.getFilterName());
      } else {
        this.rodaConfigPrefix = configPrefix;
      }
    }

    @Override
    public String getFilterName() {
      return this.filterConfig.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
      return this.filterConfig.getServletContext();
    }

    @Override
    public String getInitParameter(final String name) {
      String value = getRodaInitParameter(name);
      if (value == null) {
        value = this.filterConfig.getInitParameter(name);
      }
      return value;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      if (this.configNames == null) {
        this.configNames = new ArrayList<>();
        final Enumeration<String> filterNames = this.filterConfig.getInitParameterNames();
        while (filterNames.hasMoreElements()) {
          this.configNames.add(filterNames.nextElement());
        }
        final Iterator<String> rodaNames = this.rodaConfig.getKeys(this.rodaConfigPrefix);
        while (rodaNames.hasNext()) {
          this.configNames.add(rodaNames.next().replace(this.rodaConfigPrefix + ".", ""));
        }
      }
      return Collections.enumeration(this.configNames);
    }

    /**
     * Get filter init parameter from RODA configuration.
     *
     * @param name
     *          the parameter name.
     * @return the parameter value.
     */
    private String getRodaInitParameter(final String name) {
      return this.rodaConfig.getString(this.rodaConfigPrefix + "." + name);
    }
  }
}