package com.databasepreservation.common.client.models.status.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForeignKeysStatus implements Serializable {

  @Serial
  private static final long serialVersionUID = -4638995124950641111L;

  private String id;
  private ViewerSourceType sourceType;
  private String name;
  private String referencedTableUUID;
  private String referencedTableId;
  private List<ReferencedColumnStatus> references = new ArrayList<>();
  private VirtualForeignKeysStatus virtualForeignKeysStatus;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ViewerSourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(ViewerSourceType sourceType) {
    this.sourceType = sourceType;
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

  public List<ReferencedColumnStatus> getReferences() {
    return references;
  }

  public void setReferences(List<ReferencedColumnStatus> references) {
    this.references = references;
  }

  public VirtualForeignKeysStatus getVirtualForeignKeysStatus() {
    return virtualForeignKeysStatus;
  }

  public void setVirtualForeignKeysStatus(VirtualForeignKeysStatus virtualForeignKeysStatus) {
    this.virtualForeignKeysStatus = virtualForeignKeysStatus;
  }

  @JsonIgnore
  public boolean isVirtual() {
    return ViewerSourceType.VIRTUAL.equals(this.sourceType);
  }

  public static class ReferencedColumnStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String referencedColumnId;
    private String sourceColumnId;

    public String getReferencedColumnId() {
      return referencedColumnId;
    }

    public void setReferencedColumnId(String referencedColumnId) {
      this.referencedColumnId = referencedColumnId;
    }

    public String getSourceColumnId() {
      return sourceColumnId;
    }

    public void setSourceColumnId(String sourceColumnId) {
      this.sourceColumnId = sourceColumnId;
    }
  }
}
