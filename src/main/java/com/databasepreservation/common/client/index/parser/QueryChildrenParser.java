package com.databasepreservation.common.client.index.parser;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */

/**
 * This is a query parser which is used to query the children of documents and
 * return the parent documents.
 */
public class QueryChildrenParser extends QueryParser {
  private static final long serialVersionUID = -6394796669396577367L;

  @Override
  public String toString() {
    return "QueryChildrenParser";
  }
}
