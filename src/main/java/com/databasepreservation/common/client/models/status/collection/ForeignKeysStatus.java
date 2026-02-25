package com.databasepreservation.common.client.models.status.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.structure.ViewerType;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ForeignKeysStatus implements Serializable {

  @Serial
  private static final long serialVersionUID = -4638995124950641111L;

  private String id;
  private String name;
  private String referencedTableUUID;
  private String referencedTableId;
  private List<ReferencedColumnStatus> references = new ArrayList<>();
  private ViewerType.dbTypes type;
  private VirtualForeignKeysStatus virtualForeignKeysStatus;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public ViewerType.dbTypes getType() {
    return type;
  }

  public void setType(ViewerType.dbTypes type) {
    this.type = type;
  }

  public VirtualForeignKeysStatus getVirtualForeignKeysStatus() {
    return virtualForeignKeysStatus;
  }

  public void setVirtualForeignKeysStatus(VirtualForeignKeysStatus virtualForeignKeysStatus) {
    this.virtualForeignKeysStatus = virtualForeignKeysStatus;
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
