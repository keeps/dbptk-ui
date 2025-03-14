/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index.facets;

public class RangeFacetParameter extends FacetParameter {

  private static final long serialVersionUID = 2190074263722637169L;

  // INFO these are strings because one might pass dates or numbers
  private String start;
  private String end;
  private String gap;

  public RangeFacetParameter() {
    super();
  }

  public RangeFacetParameter(String name) {
    super(name);
  }

  public RangeFacetParameter(String name, String start, String end, String gap) {
    super(name);
    this.start = start;
    this.end = end;
    this.gap = gap;

  }

  public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public String getEnd() {
    return end;
  }

  public void setEnd(String end) {
    this.end = end;
  }

  public String getGap() {
    return gap;
  }

  public void setGap(String gap) {
    this.gap = gap;
  }

  @Override
  public String toString() {
    return "RangeFacetParameter{" + "start='" + start + '\'' + ", end='" + end + '\'' + ", gap='" + gap + '\'' + '}';
  }
}
