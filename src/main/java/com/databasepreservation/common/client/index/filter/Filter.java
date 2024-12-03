/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.index.parser.QueryParser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is a filter of data. It's used by some service methods that deal with
 * sets or lists, to filter the elements in the set or list.
 * 
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
@JsonIgnoreProperties({"returnLite"})
public class Filter implements Serializable {
  private static final long serialVersionUID = -5544859696646804386L;

  public static final Filter ALL = new Filter();
  public static final Filter NULL = null;

  private QueryParser queryParser = null;
  private List<FilterParameter> parameters = new ArrayList<>();

  /**
   * Constructs an empty {@link Filter}.
   */
  public Filter() {
    super();
  }

  /**
   * Constructs a {@link Filter} cloning an existing {@link Filter}.
   * 
   * @param filter
   *          the {@link Filter} to clone.
   */
  public Filter(Filter filter) {
    this(filter.getParameters());
  }

  /**
   * Constructs a {@link Filter} with a single parameter.
   * 
   * @param parameter
   */
  public Filter(FilterParameter parameter) {
    add(parameter);
  }

  public Filter(FilterParameter... parameters) {
    List<FilterParameter> parameterList = new ArrayList<>();
    for (FilterParameter parameter : parameters) {
      parameterList.add(parameter);
    }
    setParameters(parameterList);
  }

  /**
   * Constructs a {@link Filter} with the given parameters.
   * 
   * @param parameters
   */
  public Filter(List<FilterParameter> parameters) {
    setParameters(parameters);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "Filter [parameters=" + parameters + "; queryParser=" + queryParser + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode())
      + ((queryParser == null) ? 0 : queryParser.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Filter)) {
      return false;
    }
    Filter other = (Filter) obj;
    if (parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!parameters.equals(other.parameters)) {
      return false;
    }
    if (queryParser == null) {
      if (other.queryParser != null) {
        return false;
      }
    } else if (!queryParser.equals(other.queryParser)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the list of {@link FilterParameter}s.
   * 
   * @return an array of {@link FilterParameter} with this filter parameters.
   */
  public List<FilterParameter> getParameters() {
    return parameters;
  }

  /**
   * Sets this filter's {@link FilterParameter}s.
   * 
   * @param parameters
   *          an array of {@link FilterParameter} to set.
   */
  public void setParameters(List<FilterParameter> parameters) {
    this.parameters.clear();
    this.parameters.addAll(parameters);
  }

  /**
   * Gets the {@link QueryParser} for this filter.
   * 
   * @return an instance of {@link QueryParser} or <code>null</code> if none was
   *         set.
   */
  public QueryParser getQueryParser() {
    return queryParser;
  }

  /**
   * Sets the {@link QueryParser} for this filter.
   * 
   * @param queryParser
   *          an instance of {@link QueryParser} to set.
   */
  public void setQueryParser(QueryParser queryParser) {
    this.queryParser = queryParser;
  }

  /**
   * Adds the given parameter.
   * 
   * @param parameter
   * @return
   */
  public Filter add(FilterParameter parameter) {
    if (parameter != null) {
      this.parameters.add(parameter);
    }
    return this;
  }

  public void add(List<FilterParameter> parameters) {
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
  }

}
