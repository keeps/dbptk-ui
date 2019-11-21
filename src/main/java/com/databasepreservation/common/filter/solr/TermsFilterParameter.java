package com.databasepreservation.common.filter.solr;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.FilterParameter;

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
    return "TermsFilterParameter [getName()=" + getName() + ", getClass()=" + getClass() + ", hashCode()="
        + hashCode() + ", toString()=" + super.toString() + "]";
  }
}
