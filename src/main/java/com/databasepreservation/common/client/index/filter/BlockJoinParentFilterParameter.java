package com.databasepreservation.common.client.index.filter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BlockJoinParentFilterParameter extends FilterParameter {

  private String solrName;
  private String value;
  private String parentTableId;
  private String nestedTableId;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public BlockJoinParentFilterParameter() {
    // do nothing
  }

  public BlockJoinParentFilterParameter(BlockJoinParentFilterParameter filterParameter) {
    this(filterParameter.getSolrName(), filterParameter.getValue(), filterParameter.getParentTableId(), filterParameter.getNestedTableId());
  }

  public BlockJoinParentFilterParameter(String solrName, String value, String parentTableId, String nestedTableId) {
    setSolrName(solrName);
    setValue(value);
    setParentTableId(parentTableId);
    setNestedTableId(nestedTableId);
  }

  public String getSolrName() {
    return solrName;
  }

  public void setSolrName(String solrName) {
    this.solrName = solrName;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getParentTableId() {
    return parentTableId;
  }

  public void setParentTableId(String parentTableId) {
    this.parentTableId = parentTableId;
  }

  public String getNestedTableId() {
    return nestedTableId;
  }

  public void setNestedTableId(String nestedTableId) {
    this.nestedTableId = nestedTableId;
  }

  @Override
  public String toString() { return "{!parent which='tableId:" + parentTableId +"' filters='nestedTableId:"+ nestedTableId +"' }" + solrName + ":" + value;
  }
}
