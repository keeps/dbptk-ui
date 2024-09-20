/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.facets;

import java.util.List;

public class SimpleFacetParameter extends FacetParameter {

  private static final long serialVersionUID = -5377147008170114649L;

  public static final int DEFAULT_LIMIT = 100;
  public static final int DEFAULT_OFFSET = 0;

  private int limit = DEFAULT_LIMIT;
  private int offset = DEFAULT_OFFSET;

  public SimpleFacetParameter() {
    super();
  }

  public SimpleFacetParameter(String name) {
    super(name);
  }

  public SimpleFacetParameter(String name, int limit) {
    super(name);
    this.limit = limit;
  }

  public SimpleFacetParameter(String name, SORT sort) {
    super(name);
    this.setSort(sort);
  }

  public SimpleFacetParameter(String name, int limit, SORT sort) {
    super(name);
    this.limit = limit;
    this.setSort(sort);
  }

  public SimpleFacetParameter(String name, SORT sort, int offset) {
    super(name);
    this.setSort(sort);
    this.setOffset(offset);
  }

  public SimpleFacetParameter(String name, List<String> values) {
    super(name, values);
  }

  public SimpleFacetParameter(String name, List<String> values, int minCount) {
    super(name, values, minCount);
  }

  public SimpleFacetParameter(String name, List<String> values, int minCount, int limit) {
    super(name, values, minCount);
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    return "SimpleFacetParameter [ super=" + super.toString() + ", limit=" + this.limit + ", offset=" + this.offset + "]";
  }
}
