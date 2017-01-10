package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerDatabase implements Serializable, IsIndexed {
  private String uuid;
  private ViewerMetadata metadata;

  public ViewerDatabase() {
  }

  /**
   * @return the uuid used by solr to identify this database
   */
  @Override
  public String getUUID() {
    return uuid;
  }

  /**
   * Setter for the parameter uuid
   * 
   * @param uuid
   *          the uuid used by solr to identify this database
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * @return the database metadata
   */
  public ViewerMetadata getMetadata() {
    return metadata;
  }

  /**
   * Setter for the parameter metadata
   *
   * @param metadata
   *          the database metadata
   */
  public void setMetadata(ViewerMetadata metadata) {
    this.metadata = metadata;
  }


  @Override
  public String getId() {
    return getUUID();
  }

  @Override
  public List<String> toCsvHeaders() {
    throw new NotImplementedException("#toCsvHeaders is not implemented");
  }

  @Override
  public List<Object> toCsvValues() {
    throw new NotImplementedException("#toCsvValues is not implemented");
  }

  @Override
  public List<String> liteFields() {
    throw new NotImplementedException("#liteFields is not implemented");
  }
}
