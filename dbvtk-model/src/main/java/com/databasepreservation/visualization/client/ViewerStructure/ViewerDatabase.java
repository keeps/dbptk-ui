package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

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
    return Collections.emptyList();
  }

  @Override
  public List<Object> toCsvValues() {
    return Collections.emptyList();
  }

  @Override
  public List<String> liteFields() {
    return Collections.emptyList();
  }
}
