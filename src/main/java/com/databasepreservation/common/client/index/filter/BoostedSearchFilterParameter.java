/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.filter;

import java.io.Serial;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 * 
 */
public class BoostedSearchFilterParameter extends FilterParameter {

  @Serial
  private static final long serialVersionUID = -4480778993633875336L;

  private FilterParameter boostedFilter;
  private float boostFactor;

  /**
   * Constructs an empty {@link BoostedSearchFilterParameter}.
   */
  public BoostedSearchFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link BoostedSearchFilterParameter} cloning an existing
   * {@link BoostedSearchFilterParameter}.
   *
   * @param boostedSearchFilterParameter
   *          the {@link BoostedSearchFilterParameter} to clone.
   */
  public BoostedSearchFilterParameter(BoostedSearchFilterParameter boostedSearchFilterParameter) {
    this(boostedSearchFilterParameter.getBoostedFilter(), boostedSearchFilterParameter.getBoostFactor());
  }

  /**
   * Constructs a {@link BoostedSearchFilterParameter} with the given parameters.
   *
   * @param boostedFilter
   * @param boostFactor
   */
  public BoostedSearchFilterParameter(FilterParameter boostedFilter, float boostFactor) {
    setBoostedFilter(boostedFilter);
    setBoostFactor(boostFactor);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "BoostedSearchFilterParameter(name=" + getName() + ", value=" + getBoostedFilter() + ", boostFactor="
      + getBoostFactor() + ")";
  }

  /**
   * @return the enclosed filter parameter
   */
  public FilterParameter getBoostedFilter() {
    return boostedFilter;
  }

  /**
   * @param boostedFilter
   *          the filter parameter to set
   */
  public void setBoostedFilter(FilterParameter boostedFilter) {
    this.boostedFilter = boostedFilter;
  }

  /**
   * @return the boost factor
   */
  public float getBoostFactor() {
    return boostFactor;
  }

  /**
   * @param boostFactor
   *          the boost factor to set
   */
  public void setBoostFactor(float boostFactor) {
    this.boostFactor = boostFactor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((boostedFilter == null) ? 0 : boostedFilter.hashCode()) + Float.hashCode(boostFactor);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof BoostedSearchFilterParameter)) {
      return false;
    }
    BoostedSearchFilterParameter other = (BoostedSearchFilterParameter) obj;
    if (boostedFilter == null) {
      if (other.boostedFilter != null) {
        return false;
      }
    } else if (!boostedFilter.equals(other.boostedFilter)) {
      return false;
    }
    if (boostFactor != other.boostFactor) {
      return false;
    }
    return true;
  }
}
