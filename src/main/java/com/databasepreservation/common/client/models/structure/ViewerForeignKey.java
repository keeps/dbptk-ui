/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerForeignKey implements Serializable {
  private String name;
  private String referencedTableUUID;
  private String referencedTableId;
  private List<ViewerReference> references;
  private String matchType;
  private String deleteAction;
  private String updateAction;
  private String description;

  public ViewerForeignKey() {
  }

  public String getDeleteAction() {
    return deleteAction;
  }

  public void setDeleteAction(String deleteAction) {
    this.deleteAction = deleteAction;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMatchType() {
    return matchType;
  }

  public void setMatchType(String matchType) {
    this.matchType = matchType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReferencedTableUUID() {
    return referencedTableUUID;
  }

  public void setReferencedTableUUID(String referencedTableUUID) {
    this.referencedTableUUID = referencedTableUUID;
  }

  public String getReferencedTableId() {
    return referencedTableId;
  }

  public void setReferencedTableId(String referencedTableId) {
    this.referencedTableId = referencedTableId;
  }

  public List<ViewerReference> getReferences() {
    return references;
  }

  public void setReferences(List<ViewerReference> references) {
    this.references = references;
  }

  public String getUpdateAction() {
    return updateAction;
  }

  public void setUpdateAction(String updateAction) {
    this.updateAction = updateAction;
  }
}
