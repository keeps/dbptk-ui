package com.databasepreservation.common.client.index.filter;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BlockJoinParentFilterParameter extends FilterParameter {

  private String solrName;
  private String value;
  private String parentTableId;
  private String nestedUUID;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public BlockJoinParentFilterParameter() {
    // do nothing
  }

  public BlockJoinParentFilterParameter(BlockJoinParentFilterParameter filterParameter) {
    this(filterParameter.getSolrName(), filterParameter.getValue(), filterParameter.getParentTableId(), filterParameter.getNestedUUID());
  }

  public BlockJoinParentFilterParameter(String solrName, String value, String parentTableId, String nestedUUID) {
    setSolrName(solrName);
    setValue(value);
    setParentTableId(parentTableId);
    setNestedUUID(nestedUUID);
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

  public String getNestedUUID() {
    return nestedUUID;
  }

  public void setNestedUUID(String nestedUUID) {
    this.nestedUUID = nestedUUID;
  }

  @Override
  public String toString() { return "{!parent which='tableId:" + parentTableId +"' filters='uuid:"+ nestedUUID +"' }" + solrName + ":" + value;
  }
}
