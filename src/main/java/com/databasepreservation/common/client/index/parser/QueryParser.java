package com.databasepreservation.common.client.index.parser;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This represents the default query parser, equivalent to not using any query
 * parser in a solr query.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = DefaultQueryParser.class, name = "DefaultQueryParser"),
  @JsonSubTypes.Type(value = QueryChildrenParser.class, name = "QueryChildrenParser")})

public abstract class QueryParser implements Serializable {
  private static final long serialVersionUID = 3320943419843627896L;

  public QueryParser() {
    super();
  }

  @Override
  public String toString() {
    return "QueryParser";
  }
}
