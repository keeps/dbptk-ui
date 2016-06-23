package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerReference implements Serializable {
  private Integer sourceColumnIndex;
  private Integer referencedColumnIndex;

  /**
   * Create an empty ViewerReference
   */
  public ViewerReference() {
  }

  /**
   * @return the index of the foreign key column in the source table
   */
  public Integer getSourceColumnIndex() {
    return sourceColumnIndex;
  }

  /**
   * Set the index of the foreign key column in the source table
   * 
   * @param sourceColumnIndex
   */
  public void setSourceColumnIndex(Integer sourceColumnIndex) {
    this.sourceColumnIndex = sourceColumnIndex;
  }

  /**
   * @return the index of the referenced column in the referenced table
   */
  public Integer getReferencedColumnIndex() {
    return referencedColumnIndex;
  }

  /**
   * Set the index of the referenced column in the referenced table
   * 
   * @param referencedColumnIndex
   */
  public void setReferencedColumnIndex(Integer referencedColumnIndex) {
    this.referencedColumnIndex = referencedColumnIndex;
  }
}
