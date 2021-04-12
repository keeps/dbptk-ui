/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.filter.solr;

import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TermsFilterParameter extends FilterParameter {

  private String field;
  private String parameterValue;
  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public TermsFilterParameter() {
    // do nothing
  }

  public TermsFilterParameter(TermsFilterParameter termsFilterParameter) {
    this(termsFilterParameter.getField(), termsFilterParameter.getParameterValue());
  }

  public TermsFilterParameter(String field, String parameterValue) {
    setField(field);
    setParameterValue(parameterValue);
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getParameterValue() {
    return parameterValue;
  }

  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }

  @Override
  public String toString() {
    return "{!Terms f=" + getField() + " v=" + getParameterValue() + " }";
  }
}
